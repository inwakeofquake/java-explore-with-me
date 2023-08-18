package ru.practicum.main_service.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.main_service.dto.event.*;
import ru.practicum.main_service.entity.Category;
import ru.practicum.main_service.entity.Event;
import ru.practicum.main_service.entity.User;
import ru.practicum.main_service.enums.EventState;
import ru.practicum.main_service.enums.SortValue;
import ru.practicum.main_service.enums.StateActionForAdmin;
import ru.practicum.main_service.enums.StateActionForUser;
import ru.practicum.main_service.exceptions.BadRequestException;
import ru.practicum.main_service.exceptions.ConflictException;
import ru.practicum.main_service.exceptions.NotFoundException;
import ru.practicum.main_service.mapper.EventMapper;
import ru.practicum.main_service.repository.CategoryRepository;
import ru.practicum.main_service.repository.EventRepository;
import ru.practicum.main_service.repository.UserRepository;
import ru.practicum.main_service.service.statistics.StatisticsService;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.main_service.utility.Constants.DATE;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final EntityManager entityManager;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE);
    private final StatisticsService statisticsService;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        log.info("Attempting to create an event for user with ID: {}", userId);

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> {
                    log.error("Category with ID {} not found", newEventDto.getCategory());
                    return new NotFoundException("Category not found");
                });

        LocalDateTime eventDate = newEventDto.getEventDate();
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            log.error("Provided eventDate {} is not in the future.", eventDate);
            throw new BadRequestException("Field: eventDate. Error: must have date in the future. Value:" + eventDate);
        }

        Event event = eventMapper.toEventModel(newEventDto);
        event.setCategory(category);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Cannot create event, user with ID {} doesn't exist", userId);
                    return new NotFoundException(String.format("Can't create event, the user with id = %s doesn't exist", userId));
                });

        event.setInitiator(user);
        EventFullDto result = eventMapper.toEventFullDto(eventRepository.save(event));

        log.info("Event created successfully for user with ID: {}", userId);
        return result;
    }

    @Override
    public EventFullDto getEvent(Long id, HttpServletRequest request) {
        log.info("Fetching event by id: {}", id);
        Event event = eventRepository.findByIdAndPublishedOnIsNotNull(id)
                .orElseThrow(() -> {
                    log.warn("Event with id={} not found or not published", id);
                    return new NotFoundException(String.format("Can't find event with id = %s event doesn't exist", id));
                });
        statisticsService.setView(event);
        statisticsService.sendStat(event, request);
        return eventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getEvents(Long userId, Integer from, Integer size) {
        log.info("Fetching events for user={}, from={}, size={}", userId, from, size);
        Pageable page = PageRequest.of(from / size, size);
        List<EventShortDto> results = eventMapper.toEventShortDtoList(eventRepository.findAllByInitiatorId(userId, page).toList());
        log.info("Fetched {} events for user={}", results.size(), userId);
        return results;
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        log.info("Fetching event with id={} for user={}", eventId, userId);
        return eventMapper.toEventFullDto(eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> {
                    log.warn("Event with id={} not found for user={}", eventId, userId);
                    return new NotFoundException("Event not found");
                }));
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminDto updateEventAdminDto) {
        log.info("Attempting to update an event with ID: {}", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Event with ID {} not found", eventId);
                    return new NotFoundException(String.format("Can't update event with id = %s", eventId));
                });

        if (updateEventAdminDto == null) {
            log.warn("Received null UpdateEventAdminDto for event with ID: {}. No update performed.", eventId);
            return eventMapper.toEventFullDto(event);
        }

        if (updateEventAdminDto.getAnnotation() != null && !updateEventAdminDto.getAnnotation().isBlank()) {
            event.setAnnotation(updateEventAdminDto.getAnnotation());
        }
        if (updateEventAdminDto.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventAdminDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            event.setCategory(category);
        }
        if (updateEventAdminDto.getDescription() != null && !updateEventAdminDto.getDescription().isBlank()) {
            event.setDescription(updateEventAdminDto.getDescription());
        }
        if (updateEventAdminDto.getLocation() != null) {
            event.setLocation(updateEventAdminDto.getLocation());
        }
        if (updateEventAdminDto.getPaid() != null) {
            event.setPaid(updateEventAdminDto.getPaid());
        }
        if (updateEventAdminDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminDto.getParticipantLimit().intValue());
        }
        if (updateEventAdminDto.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminDto.getRequestModeration());
        }
        if (updateEventAdminDto.getTitle() != null && !updateEventAdminDto.getTitle().isBlank()) {
            event.setTitle(updateEventAdminDto.getTitle());
        }
        if (updateEventAdminDto.getStateAction() != null) {
            if (updateEventAdminDto.getStateAction().equals(StateActionForAdmin.PUBLISH_EVENT)) {
                if (event.getPublishedOn() != null) {
                    log.warn("Attempt to publish an already published event with ID: {}", eventId);
                    throw new ConflictException("Event already published");
                }
                if (event.getState().equals(EventState.CANCELED)) {
                    throw new ConflictException("Event already canceled");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (updateEventAdminDto.getStateAction().equals(StateActionForAdmin.REJECT_EVENT)) {
                if (event.getPublishedOn() != null) {
                    throw new ConflictException("Event already published");
                }
                event.setState(EventState.CANCELED);
            }
        }
        if (updateEventAdminDto.getEventDate() != null) {
            LocalDateTime eventDateTime = updateEventAdminDto.getEventDate();
            if (eventDateTime.isBefore(LocalDateTime.now())
                    || (event.getPublishedOn() != null && eventDateTime.isBefore(event.getPublishedOn().plusHours(1)))) {
                log.warn("Invalid event date provided for event with ID: {}", eventId);
                throw new BadRequestException("The start date of the event to be modified is less than one hour from the publication date.");
            }

            event.setEventDate(updateEventAdminDto.getEventDate());
        }

        log.info("Event with ID: {} updated successfully", eventId);
        return eventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserDto updateEventUserDto) {
        log.info("User with ID: {} attempting to update event with ID: {}", userId, eventId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> {
                    log.error("Event with ID: {} for User with ID: {} not found", eventId, userId);
                    return new NotFoundException("Event not found");
                });

        if (event.getPublishedOn() != null) {
            log.warn("Attempt to update an already published event with ID: {}", eventId);
            throw new ConflictException("Event already published");
        }

        if (updateEventUserDto == null) {
            log.warn("Received null UpdateEventUserDto for event with ID: {}. No update performed.", eventId);
            return eventMapper.toEventFullDto(event);
        }

        if (updateEventUserDto.getAnnotation() != null && !updateEventUserDto.getAnnotation().isBlank()) {
            event.setAnnotation(updateEventUserDto.getAnnotation());
        }
        if (updateEventUserDto.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventUserDto.getCategory()).orElseThrow(()
                    -> new NotFoundException("Category not found"));
            event.setCategory(category);
        }
        if (updateEventUserDto.getDescription() != null && !updateEventUserDto.getDescription().isBlank()) {
            event.setDescription(updateEventUserDto.getDescription());
        }
        if (updateEventUserDto.getEventDate() != null) {
            LocalDateTime eventDateTime = updateEventUserDto.getEventDate();
            if (eventDateTime.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException(
                        "The start date of the event to be modified is less than one hour from the publication date.");
            }
            event.setEventDate(updateEventUserDto.getEventDate());
        }
        if (updateEventUserDto.getLocation() != null) {
            event.setLocation(updateEventUserDto.getLocation());
        }
        if (updateEventUserDto.getPaid() != null) {
            event.setPaid(updateEventUserDto.getPaid());
        }
        if (updateEventUserDto.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserDto.getParticipantLimit().intValue());
        }
        if (updateEventUserDto.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserDto.getRequestModeration());
        }
        if (updateEventUserDto.getTitle() != null && !updateEventUserDto.getTitle().isBlank()) {
            event.setTitle(updateEventUserDto.getTitle());
        }

        if (updateEventUserDto.getStateAction() != null) {
            if (updateEventUserDto.getStateAction().equals(StateActionForUser.SEND_TO_REVIEW)) {
                log.info("User with ID: {} sets event with ID: {} to PENDING state", userId, eventId);
                event.setState(EventState.PENDING);
            } else {
                log.info("User with ID: {} sets event with ID: {} to CANCELED state", userId, eventId);
                event.setState(EventState.CANCELED);
            }
        }

        log.info("Event with ID: {} updated successfully by user with ID: {}", eventId, userId);
        return eventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventFullDto> getEventsWithParamsByAdmin(List<Long> users, EventState states, List<Long> categoriesId,
                                                         String rangeStart, String rangeEnd, Integer from, Integer size) {

        log.info("Fetching events with parameters: users={}, states={}, categoriesId={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                users, states, categoriesId, rangeStart, rangeEnd, from, size);

        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, dateFormatter) : null;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, dateFormatter) : null;

        if (start != null && end != null && end.isBefore(start)) {
            log.warn("Invalid date range provided: START={} cannot be after END={}", start, end);
            throw new BadRequestException("START cannot be after END");
        }

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);

        Root<Event> root = query.from(Event.class);
        Predicate criteria = builder.conjunction();

        if (categoriesId != null && !categoriesId.isEmpty()) {
            Predicate containCategories = root.get("category").in(categoriesId);
            criteria = builder.and(criteria, containCategories);
        }

        if (users != null && !users.isEmpty()) {
            Predicate containUsers = root.get("initiator").in(users);
            criteria = builder.and(criteria, containUsers);
        }

        if (states != null) {
            Predicate containStates = root.get("state").in(states);
            criteria = builder.and(criteria, containStates);
        }

        if (rangeStart != null) {
            Predicate greaterTime = builder.greaterThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), start);
            criteria = builder.and(criteria, greaterTime);
        }
        if (rangeEnd != null) {
            Predicate lessTime = builder.lessThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), end);
            criteria = builder.and(criteria, lessTime);
        }

        query.select(root).where(criteria);
        List<Event> events = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();

        if (events.isEmpty()) {
            log.info("No events found for provided parameters.");
            return new ArrayList<>();
        }

        setView(events);

        List<EventFullDto> result = eventMapper.toEventFullDtoList(events);
        log.info("Fetched {} events for provided parameters.", result.size());
        return result;
    }

    @Override
    public List<EventFullDto> getEventsWithParamsByUser(String text, List<Long> categories, Boolean paid, String rangeStart,
                                                        String rangeEnd, boolean onlyAvailable, SortValue sort,
                                                        Integer from, Integer size, HttpServletRequest request) {
        log.info("Fetching events for user with parameters: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, dateFormatter) : null;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, dateFormatter) : null;

        if (start != null && end != null && end.isBefore(start)) {
            log.warn("Invalid date range provided: Range end={} cannot be before range start={}", end, start);
            throw new BadRequestException("Range end cannot be before range start");
        }

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);

        Root<Event> root = query.from(Event.class);
        Predicate criteria = builder.conjunction();

        if (text != null && !text.isBlank()) {
            Predicate annotationContain = builder.like(builder.lower(root.get("annotation")),
                    "%" + text.toLowerCase() + "%");
            Predicate descriptionContain = builder.like(builder.lower(root.get("description")),
                    "%" + text.toLowerCase() + "%");
            Predicate containText = builder.or(annotationContain, descriptionContain);

            criteria = builder.and(criteria, containText);
        }

        if (categories != null && !categories.isEmpty()) {
            Predicate containStates = root.get("category").in(categories);
            criteria = builder.and(criteria, containStates);
        }

        if (paid != null) {
            Predicate isPaid;
            if (paid) {
                isPaid = builder.isTrue(root.get("paid"));
            } else {
                isPaid = builder.isFalse(root.get("paid"));
            }
            criteria = builder.and(criteria, isPaid);
        }

        if (rangeStart != null) {
            Predicate greaterTime = builder.greaterThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), start);
            criteria = builder.and(criteria, greaterTime);
        }
        if (rangeEnd != null) {
            Predicate lessTime = builder.lessThanOrEqualTo(root.get("eventDate").as(LocalDateTime.class), end);
            criteria = builder.and(criteria, lessTime);
        }

        query.select(root).where(criteria).orderBy(builder.asc(root.get("eventDate")));
        List<Event> events = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();

        if (onlyAvailable) {
            events = events.stream()
                    .filter((event -> event.getConfirmedRequests() < (long) event.getParticipantLimit()))
                    .collect(Collectors.toList());
        }

        if (sort != null) {
            if (sort.equals(SortValue.EVENT_DATE)) {
                events = events.stream().sorted(Comparator.comparing(Event::getEventDate)).collect(Collectors.toList());
            } else {
                events = events.stream().sorted(Comparator.comparing(Event::getViews)).collect(Collectors.toList());
            }
        }

        if (events.isEmpty()) {
            log.info("No events found for provided user parameters.");
            return new ArrayList<>();
        }

        setView(events);
        statisticsService.sendStat(events, request);

        List<EventFullDto> result = eventMapper.toEventFullDtoList(events);
        log.info("Fetched {} events for provided user parameters.", result.size());
        return result;
    }

    @Override
    public void setView(List<Event> events) {
        LocalDateTime start = LocalDateTime.of(1970, 1, 1, 1, 1);
        List<String> uris = new ArrayList<>();
        Map<String, Event> eventsUri = new HashMap<>();
        String uri = "";
        for (Event event : events) {
            if (start.isBefore(event.getCreatedOn())) {
                start = event.getCreatedOn();
            }
            uri = "/events/" + event.getId();
            uris.add(uri);
            eventsUri.put(uri, event);
            event.setViews(0L);
        }

        String startTime = start.format(DateTimeFormatter.ofPattern(DATE));
        String endTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE));

        List<ViewStatsDto> stats = statisticsService.getStats(startTime, endTime, uris);
        stats.forEach(stat ->
                eventsUri.get(stat.getUri()).setViews(stat.getHits()));
    }
}