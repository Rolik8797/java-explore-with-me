package ru.practicum.event;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.EventFilterDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ForbiddenEventException;
import ru.practicum.exception.ResourceNotFoundException;
import ru.practicum.utilities.DateFormatter;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.event.EventPrivateServiceImp.getEventStateDeterming;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventAdminServiceImp implements EventAdminService {

    private final EventRepository eventRepository;
    private final FindObjectInService findObjectInService;
    private final ProcessingEvents processingEvents;

    @Override
    public List<EventFullDto> get(EventFilterDto filterDto, HttpServletRequest request) {
        PageRequest page = PageRequest.of(filterDto.getFrom(), filterDto.getSize());
        List<Event> events;
        LocalDateTime newRangeStart = null;
        if (filterDto.getRangeStart() != null) {
            newRangeStart = DateFormatter.formatDate(filterDto.getRangeStart());
        }
        LocalDateTime newRangeEnd = null;
        if (filterDto.getRangeEnd() != null) {
            newRangeEnd = DateFormatter.formatDate(filterDto.getRangeEnd());
        }
        log.info("Получен запрос от администратора на поиск событий");
        if (filterDto.getStates() != null) {
            events = eventRepository.findAllByAdmin(filterDto.getUsers(), filterDto.getStates(), filterDto.getCategories(), newRangeStart, newRangeEnd, filterDto.getFrom(), filterDto.getSize());
        } else {
            events = eventRepository.findAllByAdminAndState(filterDto.getUsers(), null, filterDto.getCategories(), newRangeStart, newRangeEnd, page);
        }
        List<Event> eventsAddViews = processingEvents.addViewsInEventsList(events, request);
        List<Event> newEvents = processingEvents.confirmRequests(eventsAddViews);
        return newEvents.stream().map(EventMapper::eventToEventFullDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto update(Long eventId, UpdateEventAdminRequest updateEvent, HttpServletRequest request) {
        Event event = findObjectInService.getEventById(eventId);
        eventAvailability(event);
        if (updateEvent.getEventDate() != null) {
            checkEventDate(DateFormatter.formatDate(updateEvent.getEventDate()));
        }
        if (updateEvent.getAnnotation() != null && !updateEvent.getAnnotation().isBlank()) {
            event.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getCategory() != null) {
            Category category = findObjectInService.getCategoryById(updateEvent.getCategory());
            event.setCategory(category);
        }
        if (updateEvent.getDescription() != null && !updateEvent.getDescription().isBlank()) {
            event.setDescription(updateEvent.getDescription());
        }
        if (updateEvent.getEventDate() != null) {
            event.setEventDate(DateFormatter.formatDate(updateEvent.getEventDate()));
        }
        if (updateEvent.getLocation() != null) {
            event.setLocation(LocationMapper.locationDtoToLocation(updateEvent.getLocation()));
        }
        if (updateEvent.getPaid() != null) {
            event.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getRequestModeration() != null) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }
        if (updateEvent.getStateAction() != null) {
            if (!event.getState().equals(EventState.PUBLISHED) && updateEvent.getStateAction().equals(ActionState.PUBLISH_EVENT)) {
                event.setPublishedOn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
            } else if (event.getPublishedOn() == null) {
                event.setPublishedOn(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)); //????
            }
            event.setState(determiningTheStatusForEvent(updateEvent.getStateAction()));
        }
        if (updateEvent.getTitle() != null && !updateEvent.getTitle().isBlank()) {
            event.setTitle(updateEvent.getTitle());
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            long views = processingEvents.searchViews(event, request);
            event.setViews(views);
        } else {
            event.setViews(0L);
            event.setConfirmedRequests(0L);
        }
        try {
            log.info("Получен запрос от администратора на обновление события с id: {}", eventId);
            return EventMapper.eventToEventFullDto(eventRepository.save(event));
        } catch (DataAccessException e) {
            throw new ResourceNotFoundException("База данных недоступна");
        } catch (Exception e) {
            throw new BadRequestException("Запрос на добавлении события " + event + " составлен не корректно ");
        }
    }

    private void checkEventDate(LocalDateTime eventDate) {
        LocalDateTime timeNow = LocalDateTime.now().plusHours(1L);
        if (eventDate != null && eventDate.isBefore(timeNow)) {
            throw new BadRequestException("Событие должно содержать дату, которая еще не наступила. " +
                    "Текущее значение: " + eventDate);
        }
    }

    private EventState determiningTheStatusForEvent(ActionState stateAction) {
        return getEventStateDeterming(stateAction);
    }

    private void eventAvailability(Event event) {
        if (event.getState().equals(EventState.PUBLISHED) || event.getState().equals(EventState.CANCELED)) {
            throw new ForbiddenEventException("Статус события не позволяет редактировать событие, статус: " + event.getState());
        }
    }
}