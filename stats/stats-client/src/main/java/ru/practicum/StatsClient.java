package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.dto.EndpointHitRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
public class StatsClient extends BaseClient {

    private static final String APPLICATION_NAME = "ewm-main-service";

    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build()
        );
        log.info("StatsClient initialized with server URL: {}", serverUrl);
    }

    public void createHit(HttpServletRequest request) {
        final EndpointHitRequestDto hit = EndpointHitRequestDto.builder()
                .app(APPLICATION_NAME)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.from(Instant.now()))
                .build();
        log.info("Creating hit with details: {}", hit);
        post("/hit", hit);
    }
}

