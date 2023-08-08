package ru.practicum.main_service.dto.user;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NonNull
public class NewUserDto {
    @NotBlank
    private String name;
    @NotBlank
    @Email
    private String email;
}