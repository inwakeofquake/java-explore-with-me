package ru.practicum.dto;

import lombok.*;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHitResponseDto {
    private Long id;
    private String app;
    private String uri;
    private String ip;
    private Timestamp timestamp;
}
