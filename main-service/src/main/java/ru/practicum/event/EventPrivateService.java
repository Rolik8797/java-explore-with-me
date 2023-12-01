package ru.practicum.event;


import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventPrivateService {

    List<EventShortDto> get(Long userId, int from, int size, HttpServletRequest request);

    EventFullDto create(Long userId, NewEventDto newEventDto);

    EventFullDto get(Long userId, Long eventId, HttpServletRequest request);

    EventFullDto update(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest, HttpServletRequest request);

    EventRequestStatusUpdateResult updateStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequest, HttpServletRequest request);
}