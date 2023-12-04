package ru.practicum.compilation.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.practicum.event.dto.EventShortDto;

import java.util.List;

@Getter
@RequiredArgsConstructor
public final class CompilationDto {
    private final Long id;
    private final String title;
    private final Boolean pinned;
    private final List<EventShortDto> events;
}