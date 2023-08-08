package ru.practicum.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class EndpointHitDto {
    private Long id;
    @NotBlank(message = "app cannot be blank")
    private String app;
    @NotBlank(message = "uri cannot be blank")
    private String uri;
    @NotBlank(message = "ip cannot be blank")
    private String ip;
    @NotBlank(message = "timestamp cannot be blank")
    private String timestamp;
}