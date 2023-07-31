package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitRequestDto;
import ru.practicum.dto.EndpointHitResponseDto;
import ru.practicum.dto.ViewStatsResponseDto;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.service.StatsService;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.List;

@RestController
@Validated
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final StatsService statsService;
    private final StatsMapper statsMapper;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitResponseDto create(@RequestBody @Valid EndpointHitRequestDto endpointHitRequestDto) {
        log.info("Received request to create endpoint hit: {}", endpointHitRequestDto);
        EndpointHitResponseDto responseDto = statsMapper.toEndpointHitResponseDto(statsService.createHit(statsMapper.toEndpointHit(endpointHitRequestDto)));
        log.info("Endpoint hit created successfully: {}", responseDto);
        return responseDto;
    }

    @GetMapping("/stats")
    public List<ViewStatsResponseDto> getStats(
            @RequestParam Timestamp start,
            @RequestParam Timestamp end,
            @RequestParam List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique) {
        log.info("Received request to get stats from: {} to: {} for uris: {} with unique flag: {}", start, end, uris, unique);
        List<ViewStatsResponseDto> responseDtoList = statsMapper.toListViewStatsResponseDto(statsService.getStats(start, end, uris, unique));
        log.info("Stats retrieved successfully");
        return responseDtoList;
    }
}

