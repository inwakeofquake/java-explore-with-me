package ru.practicum.main_service.service.event;

import lombok.RequiredArgsConstructor;
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
import ru.practicum.main_service.exceptions.*;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;

    private final EntityManager entityManager;
    private final String datePattern = DATE;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(datePattern);
    private final StatisticsService statisticsService;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new CategoryNotExistException(""));
        LocalDateTime eventDate = newEventDto.getEventDate();
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new WrongTimeException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value:" + eventDate);
        }
        Event event = eventMapper.toEventModel(newEventDto);
        event.setCategory(category);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotExistException(String.format("Can't create event, the user with id = %s doesn't exist", userId)));
        event.setInitiator(user);
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getEvents(Long userId, Integer from, Integer size) {
        Pageable page = PageRequest.of(from / size, size);
        return eventMapper.toEventShortDtoList(eventRepository.findAllByInitiatorId(userId, page).toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminDto updateEventAdminDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotExistException(String.format("Can't update event with id = %s", eventId)));
        if (updateEventAdminDto == null) {
            return eventMapper.toEventFullDto(event);
        }

        if (updateEventAdminDto.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminDto.getAnnotation());
        }
        if (updateEventAdminDto.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventAdminDto.getCategory())
                    .orElseThrow(() -> new CategoryNotExistException(""));
            event.setCategory(category);
        }
        if (updateEventAdminDto.getDescription() != null) {
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
        if (updateEventAdminDto.getTitle() != null) {
            event.setTitle(updateEventAdminDto.getTitle());
        }
        if (updateEventAdminDto.getStateAction() != null) {
            if (updateEventAdminDto.getStateAction().equals(StateActionForAdmin.PUBLISH_EVENT)) {
                if (event.getPublishedOn() != null) {
                    throw new AlreadyPublishedException("Event already published");
                }
                if (event.getState().equals(EventState.CANCELED)) {
                    throw new EventAlreadyCanceledException("Event already canceled");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (updateEventAdminDto.getStateAction().equals(StateActionForAdmin.REJECT_EVENT)) {
                if (event.getPublishedOn() != null) {
                    throw new AlreadyPublishedException("Event already published");
                }
                event.setState(EventState.CANCELED);
            }
        }
        if (updateEventAdminDto.getEventDate() != null) {
            LocalDateTime eventDateTime = updateEventAdminDto.getEventDate();
            if (eventDateTime.isBefore(LocalDateTime.now())
                    || (event.getPublishedOn() != null && eventDateTime.isBefore(event.getPublishedOn().plusHours(1)))) {
                throw new WrongRequestArgumentException("The start date of the event to be modified is less than one hour from the publication date.");
            }

            event.setEventDate(updateEventAdminDto.getEventDate());
        }

        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserDto updateEventUserDto) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EventNotExistException(""));

        if (event.getPublishedOn() != null) {
            throw new AlreadyPublishedException("Event already published");
        }

        if (updateEventUserDto == null) {
            return eventMapper.toEventFullDto(event);
        }

        if (updateEventUserDto.getAnnotation() != null) {
            event.setAnnotation(updateEventUserDto.getAnnotation());
        }
        if (updateEventUserDto.getCategory() != null) {
            Category category = categoryRepository.findById(updateEventUserDto.getCategory()).orElseThrow(() -> new CategoryNotExistException(""));
            event.setCategory(category);
        }
        if (updateEventUserDto.getDescription() != null) {
            event.setDescription(updateEventUserDto.getDescription());
        }
        if (updateEventUserDto.getEventDate() != null) {
            LocalDateTime eventDateTime = updateEventUserDto.getEventDate();
            if (eventDateTime.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new WrongTimeException("The start date of the event to be modified is less than one hour from the publication date.");
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
        if (updateEventUserDto.getTitle() != null) {
            event.setTitle(updateEventUserDto.getTitle());
        }

        if (updateEventUserDto.getStateAction() != null) {
            if (updateEventUserDto.getStateAction().equals(StateActionForUser.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            } else {
                event.setState(EventState.CANCELED);
            }
        }

        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        return eventMapper.toEventFullDto(eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EventNotExistException("")));
    }

    @Override
    public List<EventFullDto> getEventsWithParamsByAdmin(List<Long> users, EventState states, List<Long> categoriesId,
                                                         String rangeStart, String rangeEnd, Integer from, Integer size) {
        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, dateFormatter) : null;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, dateFormatter) : null;

        if (start != null && end != null && end.isBefore(start)) {
            throw new WrongRequestArgumentException("Range end cannot be before range start");
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
            return new ArrayList<>();
        }

        setView(events);
        return eventMapper.toEventFullDtoList(events);
    }

    @Override
    public List<EventFullDto> getEventsWithParamsByUser(String text, List<Long> categories, Boolean paid, String rangeStart,
                                                        String rangeEnd, boolean onlyAvailable, SortValue sort,
                                                        Integer from, Integer size, HttpServletRequest request) {
        LocalDateTime start = rangeStart != null ? LocalDateTime.parse(rangeStart, dateFormatter) : null;
        LocalDateTime end = rangeEnd != null ? LocalDateTime.parse(rangeEnd, dateFormatter) : null;

        if (start != null && end != null && end.isBefore(start)) {
            throw new WrongRequestArgumentException("Range end cannot be before range start");
        }

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);

        Root<Event> root = query.from(Event.class);
        Predicate criteria = builder.conjunction();

        if (text != null) {
            Predicate annotationContain = builder.like(builder.lower(root.get("annotation")),
                    "%" + text.toLowerCase() + "%");
            Predicate descriptionContain = builder.like(builder.lower(root.get("description")),
                    "%" + text.toLowerCase() + "%");
            Predicate containText = builder.or(annotationContain, descriptionContain);

            criteria = builder.and(criteria, containText);
        }

        if (categories != null && categories.size() > 0) {
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
            return new ArrayList<>();
        }

        setView(events);
        statisticsService.sendStat(events, request);
        return eventMapper.toEventFullDtoList(events);
    }

    @Override
    public EventFullDto getEvent(Long id, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndPublishedOnIsNotNull(id)
                .orElseThrow(() -> new EventNotExistException(String.format(
                        "Can't find event with id = %s event doesn't exist", id)));
        statisticsService.setView(event);
        statisticsService.sendStat(event, request);
        return eventMapper.toEventFullDto(event);
    }

    @Override
    public void setView(List<Event> events) {
        LocalDateTime start = LocalDateTime.of(1970,1,1,1,1);
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
        stats.forEach((stat) ->
                eventsUri.get(stat.getUri()).setViews(stat.getHits()));
    }
}