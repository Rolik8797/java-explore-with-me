package ru.practicum;

import java.util.List;

public interface StatsService {

    EndpointHit create(EndpointHit endpointHit);

    List<ViewStats> get(String start, String end, String[] uris, Boolean unique);

}