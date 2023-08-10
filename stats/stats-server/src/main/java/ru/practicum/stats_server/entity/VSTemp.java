package ru.practicum.stats_server.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VSTemp {
    private String app;
    private String uri;
    private Long hits;
    private LocalDateTime timestamp;
}
