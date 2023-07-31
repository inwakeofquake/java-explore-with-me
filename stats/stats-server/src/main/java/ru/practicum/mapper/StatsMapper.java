package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.EndpointHitRequestDto;
import ru.practicum.dto.EndpointHitResponseDto;
import ru.practicum.dto.ViewStatsResponseDto;
import ru.practicum.entity.EndpointHit;
import ru.practicum.entity.ViewStats;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StatsMapper {
    EndpointHit toEndpointHit(EndpointHitRequestDto dto);

    EndpointHitResponseDto toEndpointHitResponseDto(EndpointHit entity);

    List<ViewStatsResponseDto> toListViewStatsResponseDto(List<ViewStats> viewStatsList);

    ViewStatsResponseDto toViewStatsResponseDto(ViewStats viewStats);
}