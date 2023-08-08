package ru.practicum.main_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.main_service.entity.Event;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findAllByInitiatorId(Long userId, Pageable page);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    List<Event> findAllByIdIn(List<Long> eventIds);

    Boolean existsByCategoryId(Long catId);

    Optional<Event> findByIdAndPublishedOnIsNotNull(Long id);
}
