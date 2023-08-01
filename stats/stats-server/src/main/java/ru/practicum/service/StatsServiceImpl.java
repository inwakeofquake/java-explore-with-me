package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.entity.EndpointHit;
import ru.practicum.entity.ViewStats;
import ru.practicum.repository.StatsRepository;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    public EndpointHit createHit(EndpointHit endpointHit) {
        log.info("Creating hit for endpoint: {}", endpointHit);
        EndpointHit result = statsRepository.save(endpointHit);
        log.info("Hit for endpoint: {} has been created successfully", endpointHit);
        return result;
    }

    @Override
    public List<ViewStats> getStats(Timestamp start, Timestamp end, List<String> uris, boolean unique) {
        log.info("Retrieving stats for the period from: {} to: {} for uris: {} with unique flag: {}", start, end, uris, unique);
        List<ViewStats> stats;
        if (Boolean.TRUE.equals(unique)) {
            if (uris == null) {
                stats = statsRepository.findStatsByDatesUniqueIpWithoutUris(start, end);
            } else {
                stats = statsRepository.findStatsByDatesUniqueIp(start, end, uris);
            }
        } else {
            if (uris == null) {
                stats = statsRepository.findStatsByDatesWithoutUris(start, end);
            } else {
                stats = statsRepository.findStatsByDates(start, end, uris);
            }
        }
        log.info("Stats retrieved successfully for the period from: {} to: {} for uris: {} with unique flag: {}", start, end, uris, unique);
        return stats;
    }
}

