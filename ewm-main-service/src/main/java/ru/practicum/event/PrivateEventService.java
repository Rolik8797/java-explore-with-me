package ru.practicum.event;


import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface PrivateEventService {

    List<EventShortDto> getEventsByPrivate(Long userId, Integer from, Integer size);

    EventFullDto getEventByPrivate(Long userId, Long eventId);

    List<ParticipationRequestDto> getRequestsByPrivate(Long userId, Long eventId);

    EventFullDto createByPrivate(NewEventDto eventDto, Long userId);

    EventFullDto updateByPrivate(UpdateEventUserRequest updateRequest, Long userId, Long eventId);

    EventRequestStatusUpdateResult updateStatusByPrivate(EventRequestStatusUpdateRequest updateRequest, Long userId,
                                                         Long eventId);
}