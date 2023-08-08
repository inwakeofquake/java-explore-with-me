package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.EndpointHit;
import ru.practicum.entity.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatRepository extends JpaRepository<EndpointHit, Long> {
    @Query("SELECT new ru.practicum.stats_server.entity.ViewStats(h.app,h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM EndpointHit h " +
            "WHERE h.timestamp BETWEEN ?1 and ?2 " +
            "AND h.uri IN (?3) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT (DISTINCT h.ip) DESC")
    List<ViewStats> findDistinctViews(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.stats_server.enity.ViewStats(h.app,h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM EndpointHit h " +
            "WHERE h.timestamp BETWEEN ?1 and ?2 " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT (DISTINCT h.ip) DESC")
    List<ViewStats> findDistinctViewsAll(LocalDateTime start, LocalDateTime end);

    @Query("SELECT new ru.practicum.stats_server.enity.ViewStats(h.app,h.uri, COUNT(h.ip)) " +
            "FROM EndpointHit h " +
            "WHERE h.timestamp BETWEEN ?1 and ?2 " +
            "AND h.uri IN (?3) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT (h.ip) DESC")
    List<ViewStats> findViews(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT new ru.practicum.stats_server.enity.ViewStats(h.app,h.uri, COUNT(h.ip)) " +
            "FROM EndpointHit h " +
            "WHERE h.timestamp BETWEEN ?1 and ?2 " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT (h.ip) DESC")
    List<ViewStats> findViewsAll(LocalDateTime start, LocalDateTime end);
}
