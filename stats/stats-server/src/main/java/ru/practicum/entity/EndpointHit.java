package ru.practicum.entity;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;

import static ru.practicum.utility.Constants.DATE;

@Entity
@Table(name = "hits")
@Data
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String app;
    private String uri;
    private String ip;
    @DateTimeFormat(pattern = DATE)
    private LocalDateTime timestamp;
}
