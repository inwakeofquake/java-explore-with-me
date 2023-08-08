package ru.practicum.main_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import ru.practicum.main_service.dto.event.EventFullDto;
import ru.practicum.main_service.dto.event.EventShortDto;
import ru.practicum.main_service.dto.event.NewEventDto;
import ru.practicum.main_service.entity.Event;

import java.util.List;


@Component
@Mapper(componentModel = "spring")
public interface EventMapper {
    EventFullDto toEventFullDto(Event event);

    @Mapping(source = "category", target = "category.id")
    Event toEventModel(NewEventDto newEventDto);

    List<EventShortDto> toEventShortDtoList(List<Event> events);

    List<EventFullDto> toEventFullDtoList(List<Event> events);
}