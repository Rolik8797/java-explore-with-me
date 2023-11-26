package ru.practicum.stats.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.EndpointHitDto;
import ru.practicum.common.dto.ViewStats;
import ru.practicum.common.formatter.DateTimeFormatter;
import ru.practicum.stats.service.StatisticService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto addHit(@Valid @RequestBody EndpointHitDto endpointHitDto) {
        log.info("POST EndpointHit {}", endpointHitDto);
        return statisticService.createHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats(
            @DateTimeFormat(pattern = DateTimeFormatter.DATE_TIME_FORMAT) @RequestParam LocalDateTime start,
            @DateTimeFormat(pattern = DateTimeFormatter.DATE_TIME_FORMAT) @RequestParam LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(required = false) boolean unique) {
        log.info("GET request at [/stats]. Params: {}, {}, {}, {}.", start, end, uris, unique);
        return statisticService.getStats(start, end, uris, unique);
    }
}