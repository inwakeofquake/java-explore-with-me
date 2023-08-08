package ru.practicum.main_service.mapper;

import org.mapstruct.Mapper;
import ru.practicum.main_service.dto.request.RequestDto;
import ru.practicum.main_service.entity.Request;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {
    RequestDto toRequestDto(Request request);

    List<RequestDto> toRequestDtoList(List<Request> requests);
}