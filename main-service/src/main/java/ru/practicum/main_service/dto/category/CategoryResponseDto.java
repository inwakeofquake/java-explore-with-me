package ru.practicum.main_service.dto.category;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CategoryResponseDto {
    private Long id;
    private String name;
}