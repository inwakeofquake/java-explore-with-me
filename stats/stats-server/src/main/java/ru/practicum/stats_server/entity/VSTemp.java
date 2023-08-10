package ru.practicum.stats_server.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static ru.practicum.stats_server.utility.Constants.DATE;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VSTemp {
    private String app;
    private String uri;
    private Long hits;
    @DateTimeFormat(pattern = DATE)
    private LocalDateTime timestamp;
}
