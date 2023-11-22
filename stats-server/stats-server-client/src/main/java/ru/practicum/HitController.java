package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;


import javax.validation.Valid;

@Controller
@RequestMapping(path = "/hit")
@RequiredArgsConstructor
@Slf4j
@Validated
public class HitController {

    private final HitClient hitClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> create(@Valid @RequestBody StatsDtoForSave endpointHit) {
        log.info("Сохранение в статистику запроса {}", endpointHit);
        return hitClient.create(endpointHit);
    }
}