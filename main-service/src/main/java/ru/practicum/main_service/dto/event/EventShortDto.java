package ru.practicum.main_service.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.main_service.dto.category.CategoryDto;
import ru.practicum.main_service.dto.user.UserShortDto;

import java.time.LocalDateTime;

import static ru.practicum.main_service.util.Constants.DATE;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EventShortDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    private Long confirmedRequests;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE)
    private LocalDateTime eventDate;
    private UserShortDto initiator;
    private Boolean paid;
    private String title;
    private Long views;
}
