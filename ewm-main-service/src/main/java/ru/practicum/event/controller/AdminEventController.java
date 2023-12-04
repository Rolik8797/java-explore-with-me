package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.AdminEventService;
import ru.practicum.event.dto.EventFilterParamsDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final AdminEventService service;

    @GetMapping
    public List<EventFullDto> getByAdmin(@Valid EventFilterParamsDto params) {
        return service.getEventsByAdmin(params);
    }

    @PatchMapping(value = "/{eventId}")
    public EventFullDto updateByAdmin(@PathVariable Long eventId,
                                      @Valid @RequestBody UpdateEventAdminRequest request) {
        return service.updateEventByAdmin(request, eventId);
    }
}