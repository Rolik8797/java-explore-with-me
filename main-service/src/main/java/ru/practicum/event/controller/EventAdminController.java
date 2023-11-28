package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.EventAdminService;
import ru.practicum.event.dto.EventFilterDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/events")
public class EventAdminController {

    private final EventAdminService eventAdminService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> get(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) String rangeStart,
            @RequestParam(required = false) String rangeEnd,
            @PositiveOrZero @RequestParam(defaultValue = "0", required = false) Integer from,
            @Positive @RequestParam(defaultValue = "10", required = false) Integer size,
            HttpServletRequest request) {

        EventFilterDto filterDto = EventFilterDto.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();

        return eventAdminService.get(filterDto, request);
    }


    @PatchMapping("/{eventId}")
    EventFullDto update(@PathVariable Long eventId,
                        @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest,
                        HttpServletRequest request) {
        return eventAdminService.update(eventId, updateEventAdminRequest, request);
    }
}