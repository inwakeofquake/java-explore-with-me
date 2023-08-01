package ru.practicum.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ViewStatsResponseDto {

    private String app;
    private String uri;
    private Long hits;
}
