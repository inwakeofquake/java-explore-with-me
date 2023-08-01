package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointHitRequestDto {
    private Long id;
    @NotBlank(message = "app cannot be blank")
    private String app;
    @NotBlank(message = "uri cannot be blank")
    private String uri;
    @NotBlank(message = "ip cannot be blank")
    private String ip;
    private LocalDateTime created;

}
