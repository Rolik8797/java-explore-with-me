package ru.practicum;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    EndpointHit create(EndpointHit endpointHit);

    List<ViewStats> get(LocalDateTime start, LocalDateTime end, String[] uris, Boolean unique);
}