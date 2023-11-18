package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHit create(@RequestBody EndpointHit endpointHit) {
        return statsService.create(endpointHit);
    }

    @GetMapping("/stats")
    public List<ViewStats> get(@RequestParam(value = "start") String start,
                               @RequestParam(value = "end") String end,
                               @RequestParam(value = "uris") String[] uris,
                               @RequestParam(value = "unique") Boolean unique) {
        return statsService.get(start, end, uris, unique);
    }
}