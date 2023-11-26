package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.dto.EndpointHitDto;
import ru.practicum.common.dto.ViewStats;
import ru.practicum.stats.mapper.EndpointMapper;
import ru.practicum.stats.repository.StatisticRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {

    private final StatisticRepository statisticRepository;

    @Override
    @Transactional
    public EndpointHitDto createHit(EndpointHitDto endpointHitDto) {
        return EndpointMapper.mapToEndpointHitDto(statisticRepository.save(EndpointMapper.mapToEndpointHit(endpointHitDto)));
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException(String
                    .format("Ошибка в интервале старта, время начала=%s не может быть меньше окончания=%s", start, end));
        }

        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return statisticRepository.getAllStatisticByDistinctIp(start, end);
            } else {
                return statisticRepository.getAllStatistic(start, end);
            }
        } else {
            if (unique) {
                return statisticRepository.getStatisticByUrisDistinctIps(uris, start, end);
            } else {
                return statisticRepository.getStatisticByUris(uris, start, end);
            }
        }
    }

}