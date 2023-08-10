package ru.practicum.stats_server.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.stats_server.entity.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ViewStatsMapper {
    List<ViewStatsDto> toEntityList(List<ViewStats> viewStats);

    Logger log = LoggerFactory.getLogger(ViewStatsMapper.class);

    ViewStatsDto toViewStatsDto(ViewStats viewStats);

    default LocalDateTime generateDateForDto() {
        return LocalDateTime.now();
    }

    @AfterMapping
    default void logAfterMapping(
            @MappingTarget ViewStatsDto dto, ViewStats viewStats,
            @Context LocalDateTime generatedDate) {
        log.info("Converted to ViewStatsDto: App: {}, URI: {}, Hits: {}, Date: {}",
                dto.getApp(), dto.getUri(), dto.getHits(), generatedDate);
    }
}
