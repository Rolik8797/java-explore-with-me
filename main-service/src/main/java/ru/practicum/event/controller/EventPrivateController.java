package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.EventPrivateService;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.request.RequestPrivateService;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class EventPrivateController {

    private final EventPrivateService eventPrivateService;
    private final RequestPrivateService requestPrivateService;

    @GetMapping()
    List<EventShortDto> get(@PathVariable Long userId,
                            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                            @Positive @RequestParam(defaultValue = "10") Integer size,
                            HttpServletRequest request) {
        return eventPrivateService.get(userId, from, size, request);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    EventFullDto create(@PathVariable Long userId,
                        @Valid @RequestBody NewEventDto newEventDto) {
        return eventPrivateService.create(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    EventFullDto get(@PathVariable Long userId,
                     @PathVariable Long eventId,
                     HttpServletRequest request) {
        return eventPrivateService.get(userId, eventId, request);
    }

    @PatchMapping("/{eventId}")
    EventFullDto update(@PathVariable Long userId,
                        @PathVariable Long eventId,
                        @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest,
                        HttpServletRequest request) {
        return eventPrivateService.update(userId, eventId, updateEventUserRequest, request);
    }

    @GetMapping("/{eventId}/requests")
    List<ParticipationRequestDto> getRequests(@PathVariable Long userId,
                                              @PathVariable Long eventId,
                                              HttpServletRequest request) {
        return requestPrivateService.getRequests(userId, eventId, request);
    }

    @PatchMapping("/{eventId}/requests")
    EventRequestStatusUpdateResult updateStatus(@PathVariable Long userId,
                                                @PathVariable Long eventId,
                                                @RequestBody EventRequestStatusUpdateRequest eventRequest,
                                                HttpServletRequest request) {
        return eventPrivateService.updateStatus(userId, eventId, eventRequest, request);
    }
}