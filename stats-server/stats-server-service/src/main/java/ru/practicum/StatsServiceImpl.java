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

        String appName = endpointHit.getAppName();
        Application application = applicationRepository.findByAppName(appName)
                .orElseGet(() -> applicationRepository.save(new Application(appName)));

        Stats stats = StatsMapper.toStats(endpointHit);
        stats.setApplication(application);
        return StatsMapper.toEndpointHit(stats);
    }

    @Override
    public List<ViewStats> get(String start, String end, String[] uris, Boolean unique) {

        LocalDateTime startTime = LocalDateTime.parse(start, formatter);
        LocalDateTime endTime = LocalDateTime.parse(end, formatter);

        if (uris[0].equals("all")) {  // Если uri изначально не был указан и ему присвоилось значение "all",
            // то выгружвется вся статистика

            if (unique) {
                return statsStorage.findStatsAllWithUniqueIp(startTime, endTime);
            } else {
                return statsStorage.findStatsAll(startTime, endTime);
            }
        } else {  // Иначе идёт выгрузка статистики согласно заданным uri
            if (unique) {
                return statsStorage.findStatsForUrisWithUniqueIp(uris, startTime, endTime);
            } else {
                return statsStorage.findStatsForUris(uris, startTime, endTime);
            }
        }
    }
}