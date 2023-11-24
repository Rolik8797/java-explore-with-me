package ru.practicum.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.model.EventForCompilation;
import ru.practicum.compilation.model.EventForCompilationPK;
import ru.practicum.event.EventService;
import ru.practicum.event.EventStorage;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.exception.NotFoundException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationStorage compilationStorage;

    private final EventForCompilationStorage eventForCompilationStorage;

    private final EventStorage eventStorage;

    private final EventService eventService;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto newCompilationDto) {

        if (newCompilationDto.getEvents() == null || newCompilationDto.getEvents().isEmpty()) {
            return CompilationMapper.toCompilationDto(compilationStorage
                    .save(CompilationMapper.toCompilation(newCompilationDto)));
        }
        Compilation compilation = compilationStorage.save(CompilationMapper.toCompilation(newCompilationDto));

        Set<EventShortDto> events = saveCompilationEvents(newCompilationDto.getEvents(), compilation.getId());

        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .events(events)
                .build();
    }

    @Override
    @Transactional
    public CompilationDto update(Integer compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationStorage.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка событий с id " + compId + " не найдена"));

        if (updateCompilationRequest.getPinned() != null || updateCompilationRequest.getTitle() != null) {
            compilationStorage.save(CompilationMapper.update(compilation, updateCompilationRequest));
        }

        Set<EventShortDto> events = new HashSet<>();
        if (updateCompilationRequest.getEvents() != null) {
            eventForCompilationStorage.deleteAllByEventForCompilationPKCompilationId(compId);

            if (!updateCompilationRequest.getEvents().contains(0)) {
                events = saveCompilationEvents(updateCompilationRequest.getEvents(), compId);
            }
        } else {
            Set<EventForCompilation> eventsForCompilation = eventForCompilationStorage
                    .findAllByEventForCompilationPKCompilationId(compId);
            for (EventForCompilation event : eventsForCompilation) {
                events.add(eventService.getById(event.getEventForCompilationPK().getEventId()));
            }
        }
        return CompilationDto.builder()
                .id(compId)
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .events(events)
                .build();
    }

    @Override
    @Transactional
    public void delete(Integer compId) {
        if (!compilationStorage.existsById(compId)) {
            throw new NotFoundException("Подборка с id " + compId + " не найдена");
        }
        eventForCompilationStorage.deleteAllByEventForCompilationPKCompilationId(compId);
        compilationStorage.deleteById(compId);
    }

    @Override
    public CompilationDto getById(Integer compId) {
        Compilation compilation = compilationStorage.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка событий с id " + compId + " не найдена"));

        Set<EventForCompilation> events = eventForCompilationStorage.findAllByEventForCompilationPKCompilationId(compId);
        Set<EventShortDto> eventShortDtoList = new HashSet<>();

        for (EventForCompilation event : events) {
            eventShortDtoList.add(eventService.getById(event.getEventForCompilationPK().getEventId()));
        }

        return CompilationDto.builder()
                .id(compId)
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .events(eventShortDtoList)
                .build();
    }

    @Override
    public List<CompilationDto> get(Boolean pinned, Integer from, Integer size) {
        List<CompilationDto> compilationDtoList = new ArrayList<>();
        Page<Compilation> compilations;
        int page = from / size;

        Pageable pageable =
                PageRequest.of(page, size);

        if (pinned != null) {
            compilations = compilationStorage.findAllByPinned(pinned, pageable);
        } else {
            compilations = compilationStorage.findAll(pageable);
        }

        if (!compilations.isEmpty()) {
            for (Compilation compilation : compilations) {
                Set<EventShortDto> events = new HashSet<>();

                Set<EventForCompilation> eventsForCompilation = eventForCompilationStorage
                        .findAllByEventForCompilationPKCompilationId(compilation.getId());
                for (EventForCompilation event : eventsForCompilation) {
                    events.add(eventService.getById(event.getEventForCompilationPK().getEventId()));
                }
                compilationDtoList.add(CompilationMapper.toCompilationDto(compilation, events));
            }
        }
        return compilationDtoList;
    }

    public Set<EventShortDto> saveCompilationEvents(Set<Integer> events, Integer compId) {
        List<EventForCompilation> eventForCompilationList = new ArrayList<>();
        Set<EventShortDto> eventShortDtoList = new HashSet<>();

        for (Integer eventId : events) {
            if (!eventStorage.existsById(eventId)) {
                throw new NotFoundException("Событие с id " + eventId + " не найдено");
            }
            eventForCompilationList.add(EventForCompilation.builder()
                    .eventForCompilationPK(EventForCompilationPK.builder()
                            .compilationId(compId)
                            .eventId(eventId)
                            .build())
                    .build());

            eventShortDtoList.add(eventService.getById(eventId));
        }
        eventForCompilationStorage.saveAll(eventForCompilationList);
        return eventShortDtoList;
    }
}