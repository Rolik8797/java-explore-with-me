package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StatisticClient {

    private static final String APP = "ewm-service";
    private static final int YEARS_OFFSET = 100;
    private final StatsServiceClient statsClient;

    public ResponseEntity<Object> saveHit(String uri, String ip) {
        EndpointHit hitRequestDto = EndpointHit.builder()
                .app(APP)
                .uri(uri)
                .ip(ip)
                .timestamp(String.valueOf(LocalDateTime.now()))
                .build();
        return statsClient.postEndpointHit(hitRequestDto);
    }

    public EventFullDto setViewsNumber(EventFullDto event) {
        List<ViewStats> hits = statsClient.getStatistic(event.getCreatedOn(), LocalDateTime.now(),
                List.of("/events/" + event.getId()), true
        );
        if (!hits.isEmpty()) {
            event.setViews(Long.valueOf(hits.get(0).getHits()));
        } else {
            event.setViews(0L);
        }
        return event;
    }

    public List<EventShortDto> setViewsNumber(List<EventShortDto> events) {
        List<String> uris = new ArrayList<>();
        for (EventShortDto eventShortDto : events) {
            uris.add("/events/" + eventShortDto.getId());
        }

        List<ViewStats> hits = statsClient.getStatistic(LocalDateTime.now().minusYears(YEARS_OFFSET),
                LocalDateTime.now(), uris, true
        );
        if (!hits.isEmpty()) {
            Map<Long, Integer> hitMap = mapHits(hits);
            for (EventShortDto event : events) {
                event.setViews(hitMap.getOrDefault(event.getId(), 0));
            }
        } else {
            for (EventShortDto event : events) {
                event.setViews(0);
            }
        }
        return events;
    }

    private Map<Long, Integer> mapHits(List<ViewStats> hits) {
        Map<Long, Integer> hitMap = new HashMap<>();
        for (var hit : hits) {
            String hitUri = hit.getUri();
            Long id = Long.valueOf(hitUri.substring(8));
            hitMap.put(id, (int) hit.getHits());
        }
        return hitMap;
    }
}