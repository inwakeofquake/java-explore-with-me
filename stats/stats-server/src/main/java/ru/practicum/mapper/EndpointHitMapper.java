package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.entity.EndpointHit;

import static ru.practicum.utility.Constants.DATE;

@Mapper(componentModel = "spring")
public interface EndpointHitMapper {
    @Mapping(target = "timestamp", source = "timestamp", dateFormat = DATE)
    EndpointHit toEntity(EndpointHitDto endpointHitDto);
}