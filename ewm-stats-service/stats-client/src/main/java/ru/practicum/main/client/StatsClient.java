package ru.practicum.main.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.practicum.common.dto.EndpointHit;
import ru.practicum.main.exception.BadRequestException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StatsClient extends BaseClient {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(serverUrl, builder);
    }

    public void addHit(String app, String uri, String ip, LocalDateTime timestamp) {
        EndpointHit endpointHit = new EndpointHit(app, uri, ip, encode(timestamp));
        makeAndSendRequest(HttpMethod.POST, "/hit", null, endpointHit);
    }

    public ResponseEntity<Object> getStats(LocalDateTime start, LocalDateTime end,
                                           @Nullable List<String> uris, @Nullable Boolean unique) {
        Map<String, Object> parameters = new HashMap<>();
        if (Objects.isNull(start) || Objects.isNull(end) || end.isBefore(start)) {
            throw new BadRequestException("Start and end shouldn't be null, and end should be after start.");
        }
        parameters.put("start", encode(start));
        parameters.put("end", encode(end));
        StringJoiner pathBuilder = new StringJoiner("&", "/stats?start={start}&end={end}", "");
        if (Objects.nonNull(uris) && !uris.isEmpty()) {
            uris.forEach(uri -> pathBuilder.add("&uris=" + uri));
        }
        if (Objects.nonNull(unique)) {
            pathBuilder.add("&unique=" + unique);
        }
        String path = pathBuilder.toString();
        return makeAndSendRequest(HttpMethod.GET, path, parameters, null);
    }

    private String encode(LocalDateTime dateTime) {
        return dateTime.format(FORMAT);
    }
}