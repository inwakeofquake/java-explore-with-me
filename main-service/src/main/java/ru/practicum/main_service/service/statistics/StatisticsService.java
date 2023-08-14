package ru.practicum.main_service.service.statistics;

import ru.practicum.dto.ViewStatsDto;
import ru.practicum.main_service.entity.Event;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsService {
    void sendStat(Event event, HttpServletRequest request);

    void sendStat(List<Event> events, HttpServletRequest request);

    void sendStatForTheEvent(Long eventId, String remoteAddr, LocalDateTime now,
                             String nameService);

    void sendStatForEveryEvent(List<Event> events, String remoteAddr, LocalDateTime now,
                               String nameService);

    void setView(Event event);

    List<ViewStatsDto> getStats(String startTime, String endTime, List<String> uris);
}
