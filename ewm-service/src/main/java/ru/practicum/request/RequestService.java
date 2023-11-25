package ru.practicum.request;

import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;


public interface RequestService {

    List<ParticipationRequestDto> getRequest(@RequestParam Long userId);

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
}