package ru.practicum.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.RequestStatus;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.ParticipationRequestMapper;
import ru.practicum.request.model.ConfirmedRequest;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.StateActionUser;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.utils.ExploreDateTimeFormatter;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements AdminEventService, PublicEventService, PrivateEventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final ParticipationRequestMapper participationRequestMapper;
    private final StatService statService;

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByPrivate(Long userId, Integer from, Integer size) {
        getUserIfExists(userId);
        int page = from / size;
        List<Event> events = eventRepository.findByInitiatorId(userId, PageRequest.of(page, size));
        statService.getViewsList(events);
        getConfirmedRequest(events);
        return new ArrayList<>(eventMapper.toEventShortDtoListForEvents(events));
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByPrivate(Long userId, Long eventId) {
        getUserIfExists(userId);
        Event event = getEventIfExists(eventId);
        getConfirmedRequest(List.of(event));
        return eventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByPrivate(Long userId, Long eventId) {
        getUserIfExists(userId);
        return requestRepository.findByEventId(eventId)
                .stream()
                .map(participationRequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto createByPrivate(NewEventDto newEventDto, Long userId) {
        Event event = eventMapper.toEvent(newEventDto);
        User user = getUserIfExists(userId);
        Category category = getCategoryIfExists(newEventDto.getCategory());
        Location location = getLocation(newEventDto.getLocation());
        event.setInitiator(user);
        event.setCategory(category);
        event.setLocation(location);
        event.setState(EventState.PENDING);
        checkDateTimeIsAfterNowWithGap(event.getEventDate(), 2);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toEventFullDto(savedEvent);
    }

    @Override
    @Transactional
    public EventFullDto updateByPrivate(UpdateEventUserRequest request, Long userId, Long eventId) {
        getUserIfExists(userId);
        Event event = getEventIfExists(eventId);
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("You cannot update a published event");
        }
        updateEventFields(event, request);
        updateEventStateAction(event, request.getStateAction());
        eventRepository.save(event);
        return eventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateStatusByPrivate(EventRequestStatusUpdateRequest update,
                                                                Long userId, Long eventId) {
        getUserIfExists(userId);
        Event event = getEventIfExists(eventId);
        List<Long> requestIds = update.getRequestIds();
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        if (!isRequestStatusUpdateAllowed(event, update)) {
            return result;
        }
        List<ParticipationRequest> requestsToUpdate = requestRepository.findAllByIdIn(requestIds);
        checkAllRequestsPending(requestsToUpdate);
        RequestStatus status = RequestStatus.valueOf(update.getStatus());
        if (status == RequestStatus.CONFIRMED) {
            confirmAndSetInResult(requestsToUpdate, result, event);
        } else if (status == RequestStatus.REJECTED) {
            rejectAndSetInResult(requestsToUpdate, result);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEventsByAdmin(EventFilterParamsDto paramsDto) {
        EventFilterParams params = convertInputParams(paramsDto);
        List<Event> events = eventRepository.adminEventsSearch(params);
        statService.getViewsList(events);
        getConfirmedRequest(events);
        return events.stream()
                .map(eventMapper::toEventFullDto)
                .sorted(getComparator(params.getSort()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(UpdateEventAdminRequest request, Long eventId) {
        Event event = getEventIfExists(eventId);
        LocalDateTime actual = event.getEventDate();
        checkDateTimeIsAfterNowWithGap(actual, 1);
        LocalDateTime target = request.getEventDate();
        if (Objects.nonNull(target)) {
            checkDateTimeIsAfterNowWithGap(target, 2);
        }
        StateActionAdmin action = request.getStateAction();
        if (Objects.nonNull(action)) {
            if (action == StateActionAdmin.PUBLISH_EVENT) {
                publishEvent(request, event);
            } else {
                rejectEvent(event);
            }
        }
        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByPublic(EventFilterParamsDto paramsDto, HttpServletRequest request) {
        EventFilterParams params = convertInputParams(paramsDto);
        List<Event> events = eventRepository.publicEventsSearch(params);
        statService.getViewsList(events);
        getConfirmedRequest(events);
        statService.addHit(request);
        return events.stream().map(eventMapper::toEventShortDto)
                .sorted(getComparator(params.getSort())).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventsByPublic(Long eventId, HttpServletRequest request) {
        Event event = getEventIfExists(eventId);
        boolean published = (event.getState() == EventState.PUBLISHED);
        if (!published) {
            throw new NotFoundException("Event not found.");
        }
        EventFullDto eventFullDto = completeEventFullDto(event);
        statService.addHit(request);
        return eventFullDto;
    }

    private boolean isRequestStatusUpdateAllowed(Event event, EventRequestStatusUpdateRequest update) {
        return event.getRequestModeration() &&
                event.getParticipantLimit() > 0 &&
                !update.getRequestIds().isEmpty();
    }

    private void completeWithViews(EventDto eventDto) {
        Long eventId = eventDto.getId();
        Long views = statService.getViews(eventId);
        eventDto.setViews(views);
    }

    private EventFilterParams convertInputParams(EventFilterParamsDto paramsDto) {
        EventFilterParams params;
        try {
            String startString = paramsDto.getRangeStart();
            String endString = paramsDto.getRangeEnd();
            LocalDateTime start = getFromStringOrSetDefault(startString, LocalDateTime.now());
            LocalDateTime end = getFromStringOrSetDefault(endString, null);
            if (end != null && end.isBefore(start)) {
                throw new BadRequestException("Invalid time-range filter params.");
            }
            params = eventMapper.toEventFilterParams(paramsDto, start, end);
        } catch (UnsupportedEncodingException e) {
            throw new ConflictException("Invalid search parameters.");
        }
        return params;
    }

    private LocalDateTime getFromStringOrSetDefault(String dateTimeString, LocalDateTime defaultValue)
            throws UnsupportedEncodingException {
        if (dateTimeString != null) {
            return ExploreDateTimeFormatter.stringToLocalDateTime(URLDecoder.decode(dateTimeString,
                    StandardCharsets.UTF_8));
        }
        return defaultValue;
    }

    private void publishEvent(UpdateEventAdminRequest request, Event event) {
        EventState state = event.getState();
        if (state == EventState.PUBLISHED) {
            throw new ConflictException("The event has already been published");
        }
        if (state == EventState.REJECTED) {
            throw new ConflictException("The event has already been rejected");
        }
        if (state == EventState.CANCELED) {
            throw new ConflictException("The event has already been canceled");
        }
        updateEventFields(event, request);
        event.setState(EventState.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
    }

    private void rejectEvent(Event event) {
        EventState state = event.getState();
        if (state == EventState.PUBLISHED || state == EventState.CANCELED) {
            throw new ConflictException("You cannot reject a published event");
        }
        event.setState(EventState.CANCELED);
    }

    private void confirmAndSetInResult(List<ParticipationRequest> requestsToUpdate,
                                       EventRequestStatusUpdateResult result, Event event) {
        long confirmed = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        long limit = event.getParticipantLimit();

        for (ParticipationRequest request : requestsToUpdate) {
            if (confirmed == limit) {
                int start = requestsToUpdate.indexOf(request);
                int end = requestsToUpdate.size();
                rejectAndSetInResult(requestsToUpdate.subList(start, end), result);
                throw new ConflictException("Participants limit is reached.");
            }
            confirmAndSetInResult(List.of(request), result);
            confirmed++;
        }
    }

    private void rejectAndSetInResult(List<ParticipationRequest> requestsToUpdate,
                                      EventRequestStatusUpdateResult result) {
        setStatus(requestsToUpdate, RequestStatus.REJECTED);
        List<ParticipationRequest> rejectedRequests = requestRepository.saveAll(requestsToUpdate);
        result.setRejectedRequests(participationRequestMapper.toRequestDtoList(rejectedRequests));
    }

    private void confirmAndSetInResult(List<ParticipationRequest> requestsToUpdate,
                                       EventRequestStatusUpdateResult result) {
        setStatus(requestsToUpdate, RequestStatus.CONFIRMED);
        List<ParticipationRequest> confirmed = requestRepository.saveAll(requestsToUpdate);
        result.setConfirmedRequests(participationRequestMapper.toRequestDtoList(confirmed));
    }

    private void setStatus(List<ParticipationRequest> requestsToUpdate, RequestStatus requestStatus) {
        requestsToUpdate.forEach(r -> r.setStatus(requestStatus));
    }

    private User getUserIfExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private Comparator<EventDto> getComparator(EventSort eventSort) {
        return EventDto.getComparator(eventSort);
    }

    private void getConfirmedRequest(List<Event> events) {
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        List<ConfirmedRequest> confirmedRequests = requestRepository.findConfirmedRequest(eventIds);
        Map<Long, Long> confirmedRequestsMap = confirmedRequests.stream()
                .collect(Collectors.toMap(ConfirmedRequest::getEventId, ConfirmedRequest::getCount));
        events.forEach(event -> event.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L)));
    }

    private Event getEventIfExists(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found."));
    }

    private Category getCategoryIfExists(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found."));
    }

    private Location getLocation(LocationDto locationDto) {
        Location location = locationMapper.toLocation(locationDto);
        return locationRepository.getByLatAndLon(location.getLat(), location.getLon())
                .orElse(locationRepository.save(location));
    }

    private void updateEventFields(Event event, UpdateEventRequest request) {
        updateEventAnnotation(event, request.getAnnotation());
        updateEventCategory(event, request.getCategory());
        updateEventDescription(event, request.getDescription());
        updateEventDate(event, request.getEventDate());
        updateEventLocation(event, request.getLocation());
        updateEventPaidStatus(event, request.getPaid());
        updateEventParticipationLimit(event, request.getParticipantLimit());
        updateEventRequestModeration(event, request.getRequestModeration());
        updateEventTitle(event, request.getTitle());
    }

    private void updateEventTitle(Event event, String title) {
        if (Objects.nonNull(title) && !title.isBlank()) {
            event.setTitle(title);
        }
    }

    private void updateEventStateAction(Event event, StateActionUser action) {
        if (Objects.nonNull(action)) {
            if (action == StateActionUser.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (action == StateActionUser.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }
    }

    private void updateEventRequestModeration(Event event, Boolean requestModeration) {
        if (Objects.nonNull(requestModeration)) {
            event.setRequestModeration(requestModeration);
        }
    }

    private void updateEventParticipationLimit(Event event, Long limit) {
        if (Objects.nonNull(limit)) {
            event.setParticipantLimit(limit);
        }
    }

    private void updateEventPaidStatus(Event event, Boolean paid) {
        if (Objects.nonNull(paid)) {
            event.setPaid(paid);
        }
    }

    private void updateEventLocation(Event event, LocationDto locationDto) {
        if (Objects.nonNull(locationDto)) {
            Location updatedLocation = getLocation(locationDto);
            event.setLocation(updatedLocation);
        }
    }

    private void updateEventDate(Event event, LocalDateTime eventDate) {
        if (Objects.nonNull(eventDate)) {
            checkDateTimeIsAfterNowWithGap(eventDate, 1);
            event.setEventDate(eventDate);
        }
    }

    private void updateEventDescription(Event event, String description) {
        if (Objects.nonNull(description) && !description.isBlank()) {
            event.setDescription(description);
        }
    }

    private void updateEventCategory(Event event, Long catId) {
        if (Objects.nonNull(catId)) {
            Category updated = getCategoryIfExists(catId);
            event.setCategory(updated);
        }
    }

    private void updateEventAnnotation(Event event, String annotation) {
        if (Objects.nonNull(annotation) && !annotation.isBlank()) {
            event.setAnnotation(annotation);
        }
    }

    private EventFullDto completeEventFullDto(Event event) {
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        Long confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        completeWithViews(eventFullDto);
        eventFullDto.setConfirmedRequests(confirmedRequests);
        return eventFullDto;
    }

    private void checkAllRequestsPending(List<ParticipationRequest> requests) {
        boolean allPending = requests.stream()
                .allMatch(r -> r.getStatus() == RequestStatus.PENDING);
        if (!allPending) {
            throw new ConflictException("Impossible to change request status.");
        }
    }

    private void checkDateTimeIsAfterNowWithGap(LocalDateTime value, Integer gapFromNowInHours) {
        LocalDateTime minValidDateTime = LocalDateTime.now().plusHours(gapFromNowInHours);
        if (value.isBefore(minValidDateTime)) {
            throw new BadRequestException("Invalid event date-time.");
        }
    }
}