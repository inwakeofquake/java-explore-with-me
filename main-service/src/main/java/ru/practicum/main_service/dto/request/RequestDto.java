package ru.practicum.main_service.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static ru.practicum.main_service.util.Constants.DATE;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RequestDto {
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE)
    private LocalDateTime created;
    private Long event;
    private Long requester;
    private String status;
}
