package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.Application;
import ru.practicum.model.Stats;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements ru.practicum.StatsService {

    private final StatsStorage statsStorage;
    private final ApplicationRepository applicationRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    @Override
    public EndpointHit create(EndpointHit endpointHit) {

        String appName = endpointHit.getApp();
        Application application = applicationRepository.findByAppName(appName)
                .orElseGet(() -> applicationRepository.save(new Application(appName)));

        Stats stats = StatsMapper.toStats(endpointHit);
        stats.setApplication(application);
        return StatsMapper.toEndpointHit(stats);
    }

    @Override
    public List<ViewStats> get(LocalDateTime start, LocalDateTime end, String[] uris, Boolean unique) {

        if (uris[0].equals("all")) {
            if (unique) {
                return statsStorage.findStatsAllWithUniqueIp(start, end);
            } else {
                return statsStorage.findStatsAll(start, end);
            }
        } else {
            if (unique) {
                return statsStorage.findStatsForUrisWithUniqueIp(uris, start, end);
            } else {
                return statsStorage.findStatsForUris(uris, start, end);
            }
        }
    }
}