package ru.practicum.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class ViewStatsDto {
    private String app;
    private String uri;
    private Long hits;
}