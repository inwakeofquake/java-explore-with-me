package ru.practicum.stats_server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stats_server.mapper.EndpointHitMapper;
import ru.practicum.stats_server.mapper.ViewStatsMapper;
import ru.practicum.stats_server.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class StatServiceImpl implements StatService {

    private final StatRepository statServerRepository;
    private final EndpointHitMapper endpointHitMapper;
    private final ViewStatsMapper viewStatsMapper;

    @Override
    public void saveHit(EndpointHitDto endpointHitDto) {
        log.debug("Attempting to save hit by app: {}", endpointHitDto.getApp());
        statServerRepository.save(endpointHitMapper.toEntity(endpointHitDto));
        log.info("Successfully saved hit for app: {}", endpointHitDto.getApp());
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.debug("Fetching stats between {} and {}. Unique? {}. Uris: {}", start, end, unique, uris);

        List<ViewStatsDto> result;

        if (unique) {
            if (uris == null || uris.isEmpty()) {
                result = statServerRepository.findDistinctViewsAll(start, end)
                        .stream()
                        .map(viewStatsMapper::toViewStatsDto).collect(Collectors.toList());
            } else {
                result = statServerRepository.findDistinctViews(start, end, uris)
                        .stream()
                        .map(viewStatsMapper::toViewStatsDto).collect(Collectors.toList());
            }
        } else {
            if (uris == null || uris.isEmpty()) {
                result = statServerRepository.findViewsAll(start, end)
                        .stream()
                        .map(viewStatsMapper::toViewStatsDto).collect(Collectors.toList());
            } else {
                result = statServerRepository.findViews(start, end, uris)
                        .stream()
                        .map(viewStatsMapper::toViewStatsDto).collect(Collectors.toList());
            }
        }
        log.info("Retrieved {} records for the provided criteria", result.size());
        return result;
    }

}
