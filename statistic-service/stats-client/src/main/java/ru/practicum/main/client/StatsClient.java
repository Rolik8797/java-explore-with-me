package ru.practicum.main.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.common.dto.EndpointHitDto;
import ru.practicum.common.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StatsClient {

    private final WebClient client;
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    public StatsClient(@Value("${statistic-service-url}") String serverUrl) {
        this.client = WebClient.create(serverUrl);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/stats")
                        .queryParam("start", start.format(formatter))
                        .queryParam("end", end.format(formatter))
                        .queryParam("uris", uris)
                        .queryParam("unique", unique)
                        .build())
                .retrieve()
                .bodyToFlux(ViewStats.class)
                .collectList()
                .block();
    }

    public void createHit(EndpointHitDto endpointHitDto) {
        client.post()
                .uri("/hit")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(endpointHitDto)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}