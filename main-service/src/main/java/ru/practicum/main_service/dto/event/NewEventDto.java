package ru.practicum.main_service.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.main_service.entity.Location;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

import static ru.practicum.main_service.util.Constants.DATE;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NewEventDto {
    @NotNull
    @Size(min = 3, max = 500)
    private String annotation;
    @NotNull
    private Long category;
    @NotNull
    @Size(min = 20, max = 5000)
    private String description;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE)
    private LocalDateTime eventDate;
    @NotNull
    private Location location;
    private boolean paid;
    private int participantLimit;
    private Boolean requestModeration;
    @NotNull
    @Size(min = 3, max = 120)
    private String title;
}
