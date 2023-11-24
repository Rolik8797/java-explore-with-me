package ru.practicum.event;

import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {

    EventFullDto create(Integer userId, NewEventDto newEventDto);

    EventFullDto getEventFullDtoByUserId(Integer userId, Integer eventId);

    List<EventShortDto> getEventShortDtosByUserId(Integer userId, Integer from, Integer size);

    EventFullDto update(Integer userId, Integer eventId, UpdateEventRequest request);

    List<EventFullDto> getByAdmin(Integer[] users, String[] states, Integer[] categories, String rangeStart,
                                  String rangeEnd, Integer from, Integer size);

    EventFullDto updateByAdmin(Integer eventId, UpdateEventRequest request);

    List<EventShortDto> getPublicly(String text, Integer[] categories, Boolean paid, String rangeStart, String rangeEnd,
                                    Boolean onlyAvailable, String sort, Integer from, Integer size,
                                    HttpServletRequest request);

    EventFullDto getPubliclyById(Integer eventId, HttpServletRequest request);

    EventShortDto getById(Integer eventId);
}