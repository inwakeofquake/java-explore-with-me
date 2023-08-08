package ru.practicum.main_service.dto.location;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationResponseDto {
    private float lat;
    private float lon;
}