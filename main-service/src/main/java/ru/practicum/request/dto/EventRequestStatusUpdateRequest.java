package ru.practicum.request.dto;

import lombok.Builder;
import lombok.Value;
import ru.practicum.request.RequestStatus;

import java.util.List;


@Value
@Builder
public class EventRequestStatusUpdateRequest {
    List<Long> requestIds;
    RequestStatus status;
}