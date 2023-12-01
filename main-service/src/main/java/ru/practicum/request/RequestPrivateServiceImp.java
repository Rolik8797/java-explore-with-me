package ru.practicum.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.EventState;
import ru.practicum.event.FindObjectInService;
import ru.practicum.event.ProcessingEvents;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictRequestException;
import ru.practicum.exception.ForbiddenEventException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestPrivateServiceImp implements RequestPrivateService {

    private final RequestRepository requestRepository;
    private final FindObjectInService findObjectInService;
    private final EventRepository eventRepository;
    private final ProcessingEvents processingEvents;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> get(Long id) {
        User user = findObjectInService.getUserById(id);
        List<Request> requests = requestRepository.findAllByRequesterIs(user);
        log.info("Получен запрос на получение всех запросов пользователя с id:" + id);
        return requests.stream().map(RequestMapper::requestToParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId, HttpServletRequest httpServletRequest) {
        User user = findObjectInService.getUserById(userId);
        Event event = findObjectInService.getEventById(eventId);
        if (event.getState().equals(EventState.PUBLISHED)) {
            addEventConfirmRequestAndSetViews(event, httpServletRequest);
        } else {
            event.setViews(0L);
            event.setConfirmedRequests(0L);
        }
        checkEventState(event);
        checkEventOwner(user, event);
        checkParticipantLimit(event);
        checkEventUser(userId, eventId);
        Request request;
        if (event.getParticipantLimit() != 0 && event.isRequestModeration()) {
            request = Request.builder()
                    .created(LocalDateTime.now())
                    .event(event)
                    .requester(user)
                    .status(RequestStatus.PENDING)
                    .build();
        } else {
            request = Request.builder()
                    .created(LocalDateTime.now())
                    .event(event)
                    .requester(user)
                    .status(RequestStatus.CONFIRMED)
                    .build();
        }
        try {
            eventRepository.save(event);
            log.info("Получен запрос на добавление запроса от пользователя с id: {} для события id: {}", userId, eventId);
            return RequestMapper.requestToParticipationRequestDto(requestRepository.save(request));
        } catch (DataAccessException e) {
            throw new BadRequestException("Ошибка при работе с базой данных");
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Ошибка в формировании запроса");
        }
    }

    @Override
    @Transactional
    public ParticipationRequestDto update(Long userId, Long requestId, HttpServletRequest httpServletRequest) {
        User user = findObjectInService.getUserById(userId);
        Request request = findObjectInService.getRequestById(requestId);
        if (!request.getRequester().equals(user)) {
            throw new ConflictRequestException("Пользователь с id: " + userId
                    + "не подавал заявку с id: " + request.getId());
        }
        request.setStatus(RequestStatus.CANCELED);
        Event event = request.getEvent();
        if (event.getState().equals(EventState.PUBLISHED)) {
            addEventConfirmRequestAndSetViews(event, httpServletRequest);
        } else {
            event.setViews(0L);
            event.setConfirmedRequests(0L);
        }
        try {
            eventRepository.save(event);
            log.info("Получен запрос на обновление запроса с id: {} от пользователя с id: {}", requestId, userId);
            return RequestMapper.requestToParticipationRequestDto(requestRepository.save(request));
        } catch (DataAccessException e) {
            throw new BadRequestException("Ошибка при работе с базой данных");
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Ошибка в формировании запроса");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequests(Long userId, Long eventId, HttpServletRequest request) {
        try {
            Event event = findObjectInService.getEventById(eventId);
            User user = findObjectInService.getUserById(userId);
            checkOwnerEvent(event, user);
            List<Request> requests = requestRepository.findAllByEvent(event);
            log.info("Получен приватный запрос на получение всех запросов для события с id: {} для пользователя с id: {}", eventId, userId);
            return requests.stream().map(RequestMapper::requestToParticipationRequestDto).collect(Collectors.toList());
        } catch (Exception e) {
            throw new BadRequestException("Некорректный запрос получения списка запросов на участие в текущем событии");
        }
    }

    private void checkOwnerEvent(Event event, User user) {
        if (!Objects.equals(event.getInitiator().getId(), user.getId())) {
            throw new ForbiddenEventException("Событие с id:" + event.getId() + " не принадлежит пользователю с id:" + user.getId());
        }
    }

    private void checkEventUser(Long userId, Long eventId) {
        Optional<Request> request = requestRepository.findByRequesterIdAndEventId(userId, eventId);
        if (request.isPresent()) {
            throw new ConflictRequestException("Пользователь с id: " + userId + "участник события с id: " + eventId);
        }
    }

    private void checkEventOwner(User user, Event event) {
        if (Objects.equals(user.getId(), event.getInitiator().getId())) {
            throw new ConflictRequestException("Пользователь с id: " + user.getId()
                    + "владелец события с id: " + event.getId() + " и не может подавать заявку на участие");
        }
    }

    private void checkEventState(Event event) {
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictRequestException("Событие с id: " + event.getId()
                    + " не опубликовано, нельзя подавать запросы на участие");
        }
    }

    private void checkParticipantLimit(Event event) {
        if (event.getParticipantLimit() == event.getConfirmedRequests() && event.getParticipantLimit() != 0) {
            throw new ConflictRequestException("Событие с id: " + event.getId()
                    + " нельзя подавать запросы на участие, превышен лимит заявок");
        }
    }

    private void addEventConfirmRequestAndSetViews(Event event, HttpServletRequest request) {
        long count = processingEvents.confirmedRequestsForOneEvent(event, RequestStatus.CONFIRMED);
        event.setConfirmedRequests(count);
        long views = processingEvents.searchViews(event, request);
        event.setViews(views);
    }
}