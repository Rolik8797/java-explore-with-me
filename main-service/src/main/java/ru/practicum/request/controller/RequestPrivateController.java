package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.RequestPrivateService;
import ru.practicum.request.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/requests")

public class RequestPrivateController {

    private final RequestPrivateService requestPrivateService;

    @GetMapping
    List<ParticipationRequestDto> get(@PathVariable Long userId) {
        return requestPrivateService.get(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ParticipationRequestDto create(@PathVariable Long userId,
                                   @NotNull @RequestParam Long eventId,
                                   HttpServletRequest request) {
        return requestPrivateService.create(userId, eventId, request);
    }

    @PatchMapping("/{requestId}/cancel")
    ParticipationRequestDto update(@PathVariable Long userId,
                                   @PathVariable Long requestId,
                                   HttpServletRequest request) {
        return requestPrivateService.update(userId, requestId, request);
    }


}