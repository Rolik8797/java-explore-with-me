package ru.practicum.event.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class EventFilterDto {

    List<Long> users;
    List<String> states;
    List<Long> categories;
    String rangeStart;
    String rangeEnd;
    int from;
    int size;

}