package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.sql.Timestamp;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotBlank(message = "timestamp cannot be blank")
    private Timestamp timestamp;

}
