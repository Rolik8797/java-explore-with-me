package ru.practicum;

import java.util.List;


import java.time.LocalDateTime;


public interface StatsService {

    void save(StatsDtoForSave statDto);

    List<StatsDtoForView> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}