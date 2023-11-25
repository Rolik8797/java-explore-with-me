package ru.practicum.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.StatisticClient;
import ru.practicum.categories.model.Categories;
import ru.practicum.categories.repository.CategoriesRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.SortEvents;
import ru.practicum.event.model.State;
import ru.practicum.event.repository.CustomBuiltEventRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ObjectNotFoundException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.ParticipationRequestStatus;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final StatisticClient statsClient;
    private final CategoriesRepository categoriesRepository;
    private final RequestRepository requestRepository;
    private final CustomBuiltEventRepository customBuiltEventRepository;
    private final LocationRepository locationRepository;

    @Override
    public List<EventShortDto> getEvents(
            String text, List<Long> categories, Boolean paid,
            LocalDateTime rangeStart, LocalDateTime rangeEnd,
            Boolean onlyAvailable, SortEvents sort, Integer from, Integer size,
            HttpServletRequest request
    ) {

        if (rangeStart != null && rangeEnd != null) {
            if (rangeStart.isAfter(rangeEnd)) {
                throw new ValidationException(String.format("Start date %s is later than end date %s", rangeStart, rangeEnd));
            }
        }

        CriteriaPublic criteria = CriteriaPublic.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .build();
        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();
        List<Event> events = customBuiltEventRepository.findEventsPublic(criteria);

        List<EventShortDto> result = events.stream().map(EventMapper::mapToShortDto).collect(Collectors.toList());

        if (!result.isEmpty()) {
            statsClient.setViewsNumber(result);

            for (EventShortDto event : result) {
                event.setConfirmedRequests(requestRepository.countAllByEventIdAndStatus(
                        event.getId(),
                        ParticipationRequestStatus.CONFIRMED
                ));
            }
        }

        statsClient.saveHit(uri, ip);

        if (!result.isEmpty()) {
            for (EventShortDto event : result) {
                statsClient.saveHit("/events/" + event.getId(), ip);
            }
        } else {
            return new ArrayList<EventShortDto>();
        }
        if (criteria.getSort() == SortEvents.VIEWS) {
            return result.stream().sorted(Comparator.comparingInt(EventShortDto::getViews)).collect(Collectors.toList());
        }

        return result.stream().sorted(Comparator.comparing(EventShortDto::getEventDate)).collect(Collectors.toList());

    }

    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new ObjectNotFoundException("Published event not found"));
        String ip = request.getRemoteAddr();
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        statsClient.saveHit("/events/" + eventId, ip);
        statsClient.setViewsNumber(eventFullDto);
        eventFullDto.setConfirmedRequests(requestRepository.countAllByEventIdAndStatus(
                event.getId(),
                ParticipationRequestStatus.CONFIRMED
        ));

        return eventFullDto;
    }

    @Override
    public List<EventFullDto> getAllEventsByUserId(Long userId, Integer from, Integer size) {
        int offset = from > 0 ? from / size : 0;
        PageRequest page = PageRequest.of(offset, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, page);
        return events.stream().map(EventMapper::toEventFullDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto createEvents(Long userId, NewEventDto newEventDto) {
        if (newEventDto.getRequestModeration() == null) {
            newEventDto.setRequestModeration(true);
        }
        LocalDateTime eventDate = newEventDto.getEventDate();
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("The event cannot occur earlier than two hours from the current moment");
        }
        User user = getUser(userId);
        Location location = getLocation(newEventDto.getLocation());
        locationRepository.save(location);
        Categories categories = getCategoriesIfExist(newEventDto.getCategory());
        Event event = EventMapper.toEvent(newEventDto, categories, location, user);
        event.setConfirmedRequests(0L);
        event.setViews(0L);
        event.setCreatedOn(LocalDateTime.now());
        Event result = eventRepository.save(event);
        return EventMapper.toEventFullDto(result);
    }

    @Override
    public EventFullDto getEventsByUserId(Long userId, Long eventId) {
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId).orElseThrow(
                () -> new ObjectNotFoundException("Event not found for user"));
        return EventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto updateEventsByUser(Long userId, Long eventId, UpdateEventRequestDto requestDto) {

        Event event = getEvents(eventId);
        if (event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("You can only change canceled events or events pending moderation");
        }
        updateEvents(event, requestDto);

        if (requestDto.getStateAction() != null) {

            switch (requestDto.getStateAction()) {
                case CANCEL_REVIEW:
                    event.setState(State.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(State.PENDING);
                    event.setPublishedOn(LocalDateTime.now());
            }
        }
        Event toUpdate = eventRepository.save(event);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(toUpdate);
        eventFullDto.setConfirmedRequests(requestRepository.countAllByEventIdAndStatus(
                event.getId(),
                ParticipationRequestStatus.CONFIRMED
        ));
        statsClient.setViewsNumber(eventFullDto);
        return eventFullDto;
    }

    @Override
    public List<ParticipationRequestDto> getRequestUserEvents(Long userId, Long eventId) {
        User user = getUser(userId);
        Event event = getEvents(eventId);

        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new ConflictException("The user is not the initiator of the event");
        }

        List<Request> requests = requestRepository.findByEventId(eventId);
        return requests.stream().map(RequestMapper::toRequestDto).collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateStatusRequestByUserIdForEvents(
            Long userId, Long eventId,
            EventRequestStatusUpdateRequest requestStatusUpdate
    ) {
        User user = getUser(userId);
        Event event = getEvents(eventId);

        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new ConflictException("The user is not the initiator of the event");
        }
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConflictException("No moderation or confirmation of applications required");
        }

        Long confirmedRequests = requestRepository.countAllByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= (confirmedRequests)) {
            throw new ConflictException("You cannot confirm an application if the limit on applications for this event has already been reached");
        }
        List<Request> requestsToUpdate = requestRepository.findAllByIdIn(requestStatusUpdate.getRequestIds());
        List<Request> confirmed = new ArrayList<>();
        List<Request> rejected = new ArrayList<>();

        for (Request request : requestsToUpdate) {

            if (!request.getStatus().equals(ParticipationRequestStatus.PENDING)) {
                continue;
            }

            if (!request.getEvent().getId().equals(eventId)) {
                rejected.add(request);
                continue;
            }
            if (requestStatusUpdate.getStatus().equals("CONFIRMED")) {
                if (confirmedRequests < event.getParticipantLimit()) {
                    request.setStatus(ParticipationRequestStatus.CONFIRMED);
                    confirmedRequests++;
                    confirmed.add(request);
                } else {
                    request.setStatus(ParticipationRequestStatus.REJECTED);
                    rejected.add(request);
                }

            } else {
                request.setStatus(ParticipationRequestStatus.REJECTED);
                rejected.add(request);
            }
        }
        eventRepository.save(event);
        requestRepository.saveAll(requestsToUpdate);

        return RequestMapper.toUpdateResultDto(confirmed, rejected);
    }

    @Override
    public List<EventFullDto> adminGetEvents(List<Long> userIds, List<State> states, List<Long> categories, String rangeStart, String rangeEnd, Integer from, Integer size) {
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (rangeStart != null) {
            start = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (rangeEnd != null) {
            end = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        if (start != null && end != null) {
            if (start.isAfter(end)) {
                throw new ValidationException("The start date cannot be after the end date");
            }
        }
        Criteria criteria = Criteria.builder()
                .users(userIds)
                .states(states)
                .categories(categories)
                .from(from)
                .size(size)
                .rangeStart(start)
                .rangeEnd(end)
                .build();
        List<Event> events = customBuiltEventRepository.getEvents(criteria);

        return events.stream().map(EventMapper::toEventFullDto)
                .map(statsClient::setViewsNumber).collect(Collectors.toList());
    }

    @Override
    public EventFullDto adminUpdateEvent(Long eventId, UpdateEventRequestDto requestDto) {
        Event event = getEvents(eventId);

        if (requestDto.getEventDate() != null && event.getPublishedOn() != null && requestDto.getEventDate().isBefore(event.getPublishedOn().plusHours(1))) {
            throw new ValidationException("The start date of the modified event must be no earlier than an hour from " +
                    "the publication date");
        }
        if (requestDto.getStateAction() != null) {

            switch (requestDto.getStateAction()) {
                case PUBLISH_EVENT:
                    if (event.getState() != State.PENDING) {
                        throw new ConflictException("The event state should be PENDING");
                    }
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    if (event.getState() == State.PUBLISHED) {
                        throw new ConflictException("Cannot cancel a published event");
                    }
                    event.setState(State.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                case CANCEL_REVIEW:
                    if (event.getState() == State.PUBLISHED) {
                        throw new ConflictException("The event status must be pending or canceled");
                    }
                    break;
            }
        }
        updateEvents(event, requestDto);
        Event toUpdate = eventRepository.save(event);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(toUpdate);
        eventFullDto.setConfirmedRequests(requestRepository.countAllByEventIdAndStatus(
                event.getId(),
                ParticipationRequestStatus.CONFIRMED
        ));
        statsClient.setViewsNumber(eventFullDto);
        return eventFullDto;
    }

    private void updateEvents(Event event, UpdateEventRequestDto requestDto) {
        if (requestDto.getAnnotation() != null) {
            event.setAnnotation(requestDto.getAnnotation());
        }
        if (requestDto.getCategory() != null) {
            Categories categories = getCategoriesIfExist(requestDto.getCategory());
            event.setCategory(categories);
        }
        if (requestDto.getDescription() != null) {
            event.setDescription(requestDto.getDescription());
        }
        if (requestDto.getEventDate() != null) {
            event.setEventDate(requestDto.getEventDate());
        }
        if (requestDto.getLocation() != null) {
            Location location = getLocation(requestDto.getLocation());
            event.setLocation(location);
        }
        if (requestDto.getPaid() != null) {
            event.setPaid(requestDto.getPaid());
        }
        if (requestDto.getParticipantLimit() != null) {
            event.setParticipantLimit(requestDto.getParticipantLimit());
        }
        if (requestDto.getRequestModeration() != null) {
            event.setRequestModeration(requestDto.getRequestModeration());
        }
        if (requestDto.getTitle() != null) {
            event.setTitle(requestDto.getTitle());
        }

    }

    private Location getLocation(LocationDto locationDto) {
        Optional<Location> location = locationRepository.findByLatAndLon(locationDto.getLat(), locationDto.getLon());

        Location savedLocation;
        if (location.isPresent()) {
            savedLocation = location.get();
            log.info("Location already exists: {}.", savedLocation);
        } else {
            savedLocation = locationRepository.save(new Location(locationDto.getLat(), locationDto.getLon()));
            log.info("Saving location: {}.", savedLocation);
        }

        return savedLocation;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("This user does not exist"));
    }

    private Event getEvents(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Such an event does not exist"));
    }

    public Categories getCategoriesIfExist(Long catId) {
        return categoriesRepository.findById(catId).orElseThrow(
                () -> new ObjectNotFoundException("Selected category not found"));
    }
}