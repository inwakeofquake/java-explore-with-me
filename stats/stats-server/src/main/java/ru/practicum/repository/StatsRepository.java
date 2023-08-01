package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.entity.EndpointHit;
import ru.practicum.entity.ViewStats;

import java.sql.Timestamp;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query(value = "SELECT new ru.practicum.entity.ViewStats(e.app, e.uri, COUNT(e.ip)) " +
            "FROM EndpointHit AS e " +
            "WHERE e.timestamp BETWEEN :start AND :end AND e.uri IN (:uris) " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(e.ip) DESC")
    List<ViewStats> findStatsByDates(Timestamp start, Timestamp end, List<String> uris);

    @Query(value = "SELECT new ru.practicum.entity.ViewStats(e.app, e.uri, COUNT(DISTINCT e.ip)) " +
            "FROM EndpointHit AS e " +
            "WHERE e.timestamp BETWEEN :start AND :end AND e.uri IN (:uris) " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(e.ip) DESC")
    List<ViewStats> findStatsByDatesUniqueIp(Timestamp start, Timestamp end, List<String> uris);

    @Query(value = "SELECT new ru.practicum.entity.ViewStats(e.app, e.uri, COUNT(e.ip)) " +
            "FROM EndpointHit AS e " +
            "WHERE e.timestamp BETWEEN :start AND :end" +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(e.ip) DESC")
    List<ViewStats> findStatsByDatesWithoutUris(Timestamp start, Timestamp end);

    @Query(value = "SELECT new ru.practicum.entity.ViewStats(e.app, e.uri, COUNT(DISTINCT e.ip)) " +
            "FROM EndpointHit AS e " +
            "WHERE e.timestamp BETWEEN :start AND :end " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY COUNT(e.ip) DESC")
    List<ViewStats> findStatsByDatesUniqueIpWithoutUris(Timestamp start, Timestamp end);
}
