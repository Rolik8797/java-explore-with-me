package ru.practicum.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.State;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ObjectNotFoundException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.ParticipationRequestStatus;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestsRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getRequest(Long userId) {
        if (userId == null) {
            throw new NullPointerException("userId cannot be null");
        }
        getUserById(userId);
        List<Request> requestsList = requestsRepository.findByRequesterId(userId);
        log.info("GET request to search about the current user's applications, with id: {}", userId);
        return requestsList.stream().map(RequestMapper::toRequestDto).collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = getUserById(userId);
        Event event = getEventsById(eventId);
        Long confirmedRequestAmount = requestsRepository.countAllByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
        if (user.getId().equals(event.getInitiator().getId())) {
            throw new ConflictException("The event initiator cannot add a request to participate in his event");
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("You cannot participate in an unpublished event");
        }
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= confirmedRequestAmount) {
            throw new ConflictException("Limit of participation requests reached");
        }
        if (requestsRepository.existsRequestByRequester_IdAndEvent_Id(userId, eventId)) {
            throw new ConflictException("The event initiator cannot add a request to participate in his event");
        }

        Request request = Request.builder()
                .requester(user)
                .event(event)
                .created(LocalDateTime.now())
                .build();
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(ParticipationRequestStatus.CONFIRMED);
        } else {
            request.setStatus(ParticipationRequestStatus.PENDING);
        }
        log.info("Create request to add a request from the current user to participate in an event, with id: {}", userId);

        return RequestMapper.toRequestDto(requestsRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        getUserById(userId);
        Request request = getRequestById(requestId);
        if (!userId.equals(request.getRequester().getId())) {
            throw new ConflictException("You can only cancel your participation request");
        }
        request.setStatus(ParticipationRequestStatus.CANCELED);
        log.info("PATH request to cancel your request to participate in the event, with id: {} {}", userId, requestId);
        return RequestMapper.toRequestDto(requestsRepository.save(request));
    }

    public User getUserById(Long userid) {
        return userRepository.findById(userid).orElseThrow(
                () -> new ObjectNotFoundException("User id not found"));

    }

    public Event getEventsById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new ObjectNotFoundException("Event not found by id"));
    }

    public Request getRequestById(Long requestId) {
        return requestsRepository.findById(requestId).orElseThrow(
                () -> new ObjectNotFoundException("Request by id not found"));
    }
}