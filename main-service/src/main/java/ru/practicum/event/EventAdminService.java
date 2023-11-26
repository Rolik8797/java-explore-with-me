package ru.practicum.event;


import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventAdminService {

    List<EventFullDto> get(List<Long> users, List<String> states, List<Long> categories,
                           String rangeStart, String rangeEnd, int from, int size, HttpServletRequest request);

    EventFullDto update(Long id, UpdateEventAdminRequest updateEventAdminRequest, HttpServletRequest request);
}