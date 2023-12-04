package ru.practicum.stats.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.dto.EndpointHit;
import ru.practicum.common.dto.StatsDto;
import ru.practicum.common.dto.ViewStats;
import ru.practicum.stats.service.StatsService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(code = HttpStatus.CREATED)
    public EndpointHit save(@RequestBody @Valid EndpointHit endpointHitDto) {
        log.info("Stats server save: {}", endpointHitDto);
        return statsService.createHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStats> get(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                               @RequestParam(required = false) List<String> uris,
                               @RequestParam(defaultValue = "false") boolean unique) {
        log.info("Stats server get: start {}, end {}, unique {}, uris is null {}", start, end, unique, uris == null);
        return statsService.getStats(StatsDto.builder().start(start).end(end).uris(uris).unique(unique).build());
    }

}