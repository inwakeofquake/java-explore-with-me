package ru.practicum.main_service.dto.user;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Builder(toBuilder = true)
@Getter
public class UserIncomeDto {
    @NotBlank
    private String name;
    @Email
    @NotEmpty
    private String email;

    public UserIncomeDto(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
