package ru.practicum.stats_server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.stats_server.entity.EndpointHit;
import ru.practicum.stats_server.utility.Constants;

@Mapper(componentModel = "spring")
public interface EndpointHitMapper {
    @Mapping(target = "timestamp", source = "timestamp", dateFormat = Constants.DATE)
    EndpointHit toEntity(EndpointHitDto endpointHitDto);
}