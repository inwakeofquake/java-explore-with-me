package ru.practicum.main_service.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static ru.practicum.main_service.util.Constants.DATE;

@Getter
@Setter
@NoArgsConstructor
public class CommentDto {

    private Long id;
    private String text;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE)
    private LocalDateTime created;
    private String authorName;
    private Long eventId;
}