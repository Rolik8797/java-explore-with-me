package ru.practicum.stats.mapper;


import ru.practicum.common.dto.EndpointHit;
import ru.practicum.common.dto.ViewStats;
import ru.practicum.stats.model.App;
import ru.practicum.stats.model.Stats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class StatsMapper {

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private StatsMapper() {
    }

    public static Stats toStats(EndpointHit endpointHitDto, App app) {
        return Stats.builder()
                .app(app)
                .uri(endpointHitDto.getUri())
                .ip(endpointHitDto.getIp())
                .timestamp(LocalDateTime.parse(endpointHitDto.getTimestamp(), formatter))
                .build();
    }

    public static EndpointHit toEndpointHitDto(Stats stats) {
        return EndpointHit.builder()
                .app(stats.getApp().getName())
                .uri(stats.getUri())
                .ip(stats.getIp())
                .timestamp(stats.getTimestamp().toString())
                .build();
    }

    public static ViewStats toViewStatsDto(ru.practicum.stats.model.ViewStats viewStats) {
        return ViewStats.builder()
                .app(viewStats.getApp())
                .uri(viewStats.getUri())
                .hits(viewStats.getHits())
                .build();
    }

    public static List<ViewStats> toViewStatsDtos(List<ru.practicum.stats.model.ViewStats> viewStats) {
        return viewStats.stream().map(StatsMapper::toViewStatsDto).collect(Collectors.toList());
    }

}