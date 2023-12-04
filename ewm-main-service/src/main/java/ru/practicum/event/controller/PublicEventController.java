package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.event.PublicEventService;
import ru.practicum.event.dto.EventFilterParamsDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final PublicEventService service;

    @GetMapping
    public List<EventShortDto> get(@Valid EventFilterParamsDto params, HttpServletRequest request) {
        return service.getEventsByPublic(params, request);
    }

    @GetMapping(value = "/{id}")
    public EventFullDto get(@PathVariable Long id, HttpServletRequest request) {
        return service.getEventsByPublic(id, request);
    }
}