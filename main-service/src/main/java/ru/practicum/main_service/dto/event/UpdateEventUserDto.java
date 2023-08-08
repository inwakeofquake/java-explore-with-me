package ru.practicum.main_service.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.main_service.entity.Location;
import ru.practicum.main_service.enums.StateActionForUser;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

import static ru.practicum.main_service.util.Constants.DATE;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateEventUserDto {
    @Size(min = 3, max = 500)
    private String annotation;
    private Long category;
    @Size(min = 20, max = 2000)
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE)
    private LocalDateTime eventDate;
    private Location location;
    private Boolean paid;
    private Long participantLimit;
    private Boolean requestModeration;
    private StateActionForUser stateAction;
    @Size(min = 2, max = 120)
    private String title;
}

