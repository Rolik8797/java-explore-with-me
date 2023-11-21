package ru.practicum.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.EndpointHit;
import ru.practicum.model.Application;
import ru.practicum.model.Stats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StatsMapper {

    public static final DateTimeFormatter DATEFORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Stats toStats(EndpointHit endpointHit) {
        Integer id = endpointHit.getId() != null ? endpointHit.getId() : null;
        LocalDateTime timestamp = endpointHit.getTimestamp() != null
                ? LocalDateTime.parse(endpointHit.getTimestamp(), DATEFORMATTER)
                : LocalDateTime.now();

        String appName = endpointHit.getApp();
        String appNameFromHit = endpointHit.getAppName(); // Поменяйте на getAppName
        Application application = appNameFromHit != null ? new Application(appNameFromHit) : new Application();

        return Stats.builder()
                .id(id)
                .application(application)
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(timestamp)
                .build();
    }

    public static EndpointHit toEndpointHit(Stats stats) {
        String formattedTimestamp = stats.getTimestamp() != null ? stats.getTimestamp().format(DATEFORMATTER) : null;

        return EndpointHit.builder()
                .id(stats.getId())
                .app(stats.getApplication().getAppName())
                .appName(stats.getApplication().getAppName())
                .uri(stats.getUri())
                .ip(stats.getIp())
                .timestamp(formattedTimestamp)
                .build();
    }

    public static Stats toStats(EndpointHit endpointHit, Application application) {
        Stats stats = new Stats();
        stats.setApplication(application); // Set the Application instance
        stats.setIp(endpointHit.getIp());
        stats.setTimestamp(LocalDateTime.parse(endpointHit.getTimestamp(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        stats.setUri(endpointHit.getUri());
        return stats;
    }
}