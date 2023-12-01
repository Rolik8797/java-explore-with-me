package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.dto.EndpointHitDto;
import ru.practicum.common.dto.StatsDto;
import ru.practicum.common.dto.ViewStatsDto;
import ru.practicum.stats.exception.AppNotFoundException;
import ru.practicum.stats.mapper.StatsMapper;
import ru.practicum.stats.model.App;
import ru.practicum.stats.model.Stats;
import ru.practicum.stats.model.ViewStats;
import ru.practicum.stats.repository.AppRepository;
import ru.practicum.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;
    private final AppRepository appRepository;

    @Override
    @Transactional
    public EndpointHitDto createHit(EndpointHitDto endpointHitDto) {
        String nameApp = endpointHitDto.getApp();
        Optional<App> appName = appRepository.findByName(nameApp);
        App app;
        if (appName.isEmpty()) {
            app = appRepository.save(new App(nameApp));
        } else {
            app = appName.orElseThrow(() -> new AppNotFoundException("App not found: " + nameApp));
        }
        Stats saved = statsRepository.save(StatsMapper.toStats(endpointHitDto, app));
        return StatsMapper.toEndpointHitDto(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ViewStatsDto> getStats(StatsDto statsDto) {
        List<ViewStats> stats;
        Boolean unique = statsDto.getUnique();
        List<String> uris = statsDto.getUris();
        LocalDateTime start = statsDto.getStart();
        LocalDateTime end = statsDto.getEnd();
        if (end.isBefore(start)) {
            throw new IllegalArgumentException(String
                    .format("Ошибка в интервале старта, время начала=%s не может быть меньше окончания=%s", start, end));
        }
        if (unique) {
            if (uris == null) {
                stats = statsRepository.findStatsUniqueWithOutUris(start, end);
            } else {
                stats = statsRepository.findStatsUnique(start, end, uris);
            }
        } else {
            if (uris == null) {
                stats = statsRepository.findStatsWithOutUris(start, end);
            } else {
                stats = statsRepository.findStats(start, end, uris);
            }
        }
        return StatsMapper.toViewStatsDtos(stats);
    }

}