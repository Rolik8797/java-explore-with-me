package ru.practicum.stats.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.common.dto.EndpointHitDto;
import ru.practicum.common.formatter.DateTimeFormatter;
import ru.practicum.stats.model.EndpointHit;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EndpointMapper {

    public static EndpointHit mapToEndpointHit(EndpointHitDto endpointHitDto) {
        return EndpointHit.builder()
                .app(endpointHitDto.getApp())
                .ip(endpointHitDto.getIp())
                .uri(endpointHitDto.getUri())
                .timestamp(LocalDateTime.parse(endpointHitDto.getTimestamp(), DateTimeFormatter.DATE_TIME_FORMATTER))
                .build();
    }

    public static EndpointHitDto mapToEndpointHitDto(EndpointHit endpointHit) {
        return EndpointHitDto.builder()
                .app(endpointHit.getApp())
                .ip(endpointHit.getIp())
                .uri(endpointHit.getUri())
                .timestamp(endpointHit.getTimestamp().format(DateTimeFormatter.DATE_TIME_FORMATTER))
                .build();
    }
}