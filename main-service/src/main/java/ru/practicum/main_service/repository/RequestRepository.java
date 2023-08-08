package ru.practicum.main_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.main_service.entity.Request;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    @Query("select p from Request as p " +
            "join Event as e ON p.event = e.id " +
            "where p.event = :eventId and e.initiator.id = :userId")
    List<Request> findAllByEventWithInitiator(@Param(value = "userId") Long userId,
                                              @Param("eventId") Long eventId);

    Boolean existsByRequesterAndEvent(Long userId, Long eventId);

    List<Request> findAllByRequester(Long userId);

    List<Request> findAllByEvent(Long eventId);

    Optional<Request> findByRequesterAndId(Long userId, Long requestId);
}
