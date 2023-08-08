package ru.practicum.main_service.service.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.main_service.dto.compilation.CompilationDto;
import ru.practicum.main_service.dto.compilation.NewCompilationDto;
import ru.practicum.main_service.dto.compilation.UpdateCompilationRequest;
import ru.practicum.main_service.entity.Compilation;
import ru.practicum.main_service.entity.Event;
import ru.practicum.main_service.exceptions.CompilationNotExistException;
import ru.practicum.main_service.mapper.CompilationMapper;
import ru.practicum.main_service.repository.CompilationRepository;
import ru.practicum.main_service.repository.EventRepository;
import ru.practicum.main_service.service.event.EventService;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final EventRepository eventRepository;
    private final EventService eventService;
    private final EntityManager entityManager;
    private final CompilationRepository compilationRepository;
    private final CompilationMapper mapper;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        List<Event> events = eventRepository.findAllByIdIn(newCompilationDto.getEvents());
        Compilation compilation = new Compilation();
        compilation.setEvents(new HashSet<>(events));
        compilation.setPinned(newCompilationDto.getPinned());
        compilation.setTitle(newCompilationDto.getTitle());

        Compilation savedCompilation = compilationRepository.save(compilation);
        log.debug("Compilation is created");
        setView(savedCompilation);
        return mapper.mapToCompilationDto(savedCompilation);
    }

    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotExistException("Compilation doesn't exist"));
        return mapper.mapToCompilationDto(compilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Compilation> query = builder.createQuery(Compilation.class);

        Root<Compilation> root = query.from(Compilation.class);
        Predicate criteria = builder.conjunction();

        if (pinned != null) {
            Predicate isPinned;
            if (pinned) {
                isPinned = builder.isTrue(root.get("pinned"));
            } else {
                isPinned = builder.isFalse(root.get("pinned"));
            }
            criteria = builder.and(criteria, isPinned);
        }

        query.select(root).where(criteria);
        List<Compilation> compilations = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();

        return mapper.mapToListCompilationDto(compilations);
    }

    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {

        Compilation oldCompilation = compilationRepository.findById(compId).orElseThrow(() -> new CompilationNotExistException("Can't update compilation - the compilation doesn't exist"));
        List<Long> eventsIds = updateCompilationRequest.getEvents();
        if (eventsIds != null) {
            List<Event> events = eventRepository.findAllByIdIn(updateCompilationRequest.getEvents());
            oldCompilation.setEvents(new HashSet<>(events));
        }
        if (updateCompilationRequest.getPinned() != null) {
            oldCompilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            oldCompilation.setTitle(updateCompilationRequest.getTitle());
        }
        Compilation updatedCompilation = compilationRepository.save(oldCompilation);
        log.debug("Compilation with ID = {} is updated", compId);
        setView(updatedCompilation);
        return mapper.mapToCompilationDto(updatedCompilation);
    }

    @Transactional
    public void deleteCompilation(Long compId) {
        compilationRepository.deleteById(compId);
        log.debug("Compilation with ID = {} is deleted", compId);
    }

    private void setView(Compilation compilation) {
        Set<Event> setEvents = compilation.getEvents();
        if (!setEvents.isEmpty()) {
            List<Event> events = new ArrayList<>(setEvents);
            eventService.setView(events);
        }
    }
}