package ru.practicum.main_service.service.statistics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.main_service.entity.Event;
import ru.practicum.stats_client.StatClient;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ru.practicum.main_service.utility.Constants.DATE;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final StatClient statClient;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE);

    @Override
    public void sendStat(Event event, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String remoteAddr = request.getRemoteAddr();
        String nameService = "main-service";

        EndpointHitDto requestDto = new EndpointHitDto();
        requestDto.setTimestamp(now.format(dateFormatter));
        requestDto.setUri("/events");
        requestDto.setApp(nameService);
        requestDto.setIp(remoteAddr);
        statClient.addStats(requestDto);
        log.info("Stat sent for the general event endpoint from IP: {}", remoteAddr);
        sendStatForTheEvent(event.getId(), remoteAddr, now, nameService);
    }

    @Override
    public void sendStat(List<Event> events, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String remoteAddr = request.getRemoteAddr();
        String nameService = "main-service";

        EndpointHitDto requestDto = new EndpointHitDto();
        requestDto.setTimestamp(now.format(dateFormatter));
        requestDto.setUri("/events");
        requestDto.setApp(nameService);
        requestDto.setIp(request.getRemoteAddr());
        statClient.addStats(requestDto);
        log.info("Stat sent for multiple events from IP: {}", remoteAddr);
        sendStatForEveryEvent(events, remoteAddr, LocalDateTime.now(), nameService);
    }

    @Override
    public void sendStatForTheEvent(Long eventId, String remoteAddr, LocalDateTime now,
                                    String nameService) {
        EndpointHitDto requestDto = new EndpointHitDto();
        requestDto.setTimestamp(now.format(dateFormatter));
        requestDto.setUri("/events/" + eventId);
        requestDto.setApp(nameService);
        requestDto.setIp(remoteAddr);
        statClient.addStats(requestDto);
        log.info("Stat sent for event with ID {} from IP: {}", eventId, remoteAddr);
    }

    @Override
    public void sendStatForEveryEvent(List<Event> events, String remoteAddr, LocalDateTime now,
                                      String nameService) {
        for (Event event : events) {
            EndpointHitDto requestDto = new EndpointHitDto();
            requestDto.setTimestamp(now.format(dateFormatter));
            requestDto.setUri("/events/" + event.getId());
            requestDto.setApp(nameService);
            requestDto.setIp(remoteAddr);
            statClient.addStats(requestDto);
            log.info("Stats sent for {} events from IP: {}", events.size(), remoteAddr);
        }
    }

    @Override
    public void setView(Event event) {
        String startTime = LocalDateTime.of(1970, 1, 1, 1, 1).format(dateFormatter);
        String endTime = LocalDateTime.now().format(dateFormatter);
        List<String> uris = List.of("/events/" + event.getId());

        List<ViewStatsDto> stats = getStats(startTime, endTime, uris);
        if (stats.size() == 1) {
            event.setViews(stats.get(0).getHits());
            log.info("Views set for event with ID {}: {}", event.getId(), stats.get(0).getHits());
        } else {
            event.setViews(0L);
            log.error("No stats found for event with ID {}", event.getId());
        }
    }

    @Override
    public List<ViewStatsDto> getStats(String startTime, String endTime, List<String> uris) {
        log.info("Fetching stats from {} to {} for URIs: {}", startTime, endTime, uris);
        return statClient.getStats(startTime, endTime, uris, true);
    }
}