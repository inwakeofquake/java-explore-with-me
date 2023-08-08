package ru.practicum.main_service.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.main_service.dto.category.CategoryDto;
import ru.practicum.main_service.dto.user.UserShortDto;
import ru.practicum.main_service.entity.Location;
import ru.practicum.main_service.enums.EventState;

import java.time.LocalDateTime;

import static ru.practicum.main_service.util.Constants.DATE;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EventFullDto {
    private String annotation;
    private CategoryDto category;
    private Integer confirmedRequests;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE)
    private String createdOn;
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE)
    private LocalDateTime eventDate;
    private Long id;
    private UserShortDto initiator;
    private Location location;
    private Boolean paid;
    private Long participantLimit;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE)
    private LocalDateTime publishedOn;
    private Boolean requestModeration;
    private EventState state;
    private String title;
    private Long views;
}