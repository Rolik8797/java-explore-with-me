package ru.practicum.stats.service;


import ru.practicum.common.dto.EndpointHit;
import ru.practicum.common.dto.StatsDto;
import ru.practicum.common.dto.ViewStats;

import java.util.List;

public interface StatsService {

    EndpointHit createHit(EndpointHit endpointHitDto);

    List<ViewStats> getStats(StatsDto statsDto);

}