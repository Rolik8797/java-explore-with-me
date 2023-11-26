package ru.practicum.compilation.dto;

import lombok.Builder;
import lombok.Value;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

@Value
@Builder
public class CompilationDto {
    List<EventShortDto> events;
    Long id;
    boolean pinned;
    String title;
}