package ru.practicum.request;


import ru.practicum.request.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface RequestPrivateService {

    List<ParticipationRequestDto> get(Long id);

    ParticipationRequestDto create(Long userId, Long eventId, HttpServletRequest request);

    ParticipationRequestDto update(Long userId, Long requestId, HttpServletRequest request);

    List<ParticipationRequestDto> getRequests(Long userId, Long eventId, HttpServletRequest request);

}