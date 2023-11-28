package ru.practicum.event;


import ru.practicum.event.dto.EventFilterDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventAdminService {

    List<EventFullDto> get(EventFilterDto filterDto, HttpServletRequest request);

    EventFullDto update(Long id, UpdateEventAdminRequest updateEventAdminRequest, HttpServletRequest request);
}