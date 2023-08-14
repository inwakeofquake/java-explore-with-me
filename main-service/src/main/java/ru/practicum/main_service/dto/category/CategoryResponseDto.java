package ru.practicum.main_service.dto.category;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
public class CategoryResponseDto {
    private Long id;
    @NotBlank
    private String name;
}