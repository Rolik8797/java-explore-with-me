package ru.practicum.event;


import ru.practicum.event.dto.EventFilterParamsDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;

import java.util.List;

public interface AdminEventService {

    List<EventFullDto> getEventsByAdmin(EventFilterParamsDto params);

    EventFullDto updateEventByAdmin(UpdateEventAdminRequest request, Long eventId);
}