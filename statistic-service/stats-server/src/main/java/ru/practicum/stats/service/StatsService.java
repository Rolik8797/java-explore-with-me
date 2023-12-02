package ru.practicum.stats.service;


import ru.practicum.common.dto.EndpointHitDto;
import ru.practicum.common.dto.StatsDto;
import ru.practicum.common.dto.ViewStatsDto;

import java.util.List;

public interface StatsService {

    EndpointHitDto createHit(EndpointHitDto endpointHitDto);

    List<ViewStatsDto> getStats(StatsDto statsDto);

}