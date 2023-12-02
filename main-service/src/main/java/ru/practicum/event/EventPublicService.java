package ru.practicum.event;

import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventPublicService {

    List<EventShortDto> get(String text, List<Long> categories, Boolean paid, String rangeStart, String rangeEnd,
                            boolean onlyAvailable, String sort, Integer from, Integer size, HttpServletRequest request);


    EventFullDto get(Long id, HttpServletRequest request);
}