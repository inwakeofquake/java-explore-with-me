package ru.practicum.main_service.dto.compilation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.main_service.dto.event.EventShortDto;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CompilationDto {
    private Long id;
    private Boolean pinned;
    private String title;
    private List<EventShortDto> events;
}
