package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.model.Application;


import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;
    private final ApplicationRepository applicationRepository;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHit create(@RequestBody EndpointHit endpointHit) {
        return statsService.create(endpointHit);
    }

    @GetMapping("/stats")
    public List<ViewStats> get(@RequestParam(value = "start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String start,
                               @RequestParam(value = "end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") String end,
                               @RequestParam(value = "uris") String[] uris,
                               @RequestParam(value = "unique") Boolean unique) {
        return statsService.get(start, end, uris, unique);
    }

    @GetMapping("/applications")
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    @GetMapping("/applications/{appName}")
    public Application getApplicationByName(@PathVariable String appName) {
        return applicationRepository.findByAppName(appName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
    }
}