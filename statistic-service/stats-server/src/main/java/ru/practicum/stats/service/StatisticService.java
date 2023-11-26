package ru.practicum.stats.service;

import ru.practicum.common.dto.EndpointHitDto;
import ru.practicum.common.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticService {

    EndpointHitDto createHit(EndpointHitDto endpointHitDto);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}