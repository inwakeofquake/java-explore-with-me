package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.entity.EndpointHit;
import ru.practicum.entity.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatRepository extends JpaRepository<EndpointHit, Long> {
    @Query("SELECT new ru.practicum.stats_server.entity.ViewStats(h.app,h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM EndpointHit h " +
            "WHERE h.timestamp BETWEEN :start and :end " +
            "AND h.uri IN (:uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT (DISTINCT h.ip) DESC")
    List<ViewStats> findDistinctViews(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris);

    @Query("SELECT new ru.practicum.stats_server.entity.ViewStats(h.app,h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM EndpointHit h " +
            "WHERE h.timestamp BETWEEN :start and :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT (DISTINCT h.ip) DESC")
    List<ViewStats> findDistinctViewsAll(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT new ru.practicum.stats_server.entity.ViewStats(h.app,h.uri, COUNT(h.ip)) " +
            "FROM EndpointHit h " +
            "WHERE h.timestamp BETWEEN :start and :end " +
            "AND h.uri IN (:uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT (h.ip) DESC")
    List<ViewStats> findViews(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris);

    @Query("SELECT new ru.practicum.stats_server.entity.ViewStats(h.app,h.uri, COUNT(h.ip)) " +
            "FROM EndpointHit h " +
            "WHERE h.timestamp BETWEEN :start and :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT (h.ip) DESC")
    List<ViewStats> findViewsAll(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
