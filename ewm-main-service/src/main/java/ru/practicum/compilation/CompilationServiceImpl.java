package ru.practicum.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;


    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getAll(Boolean pinned, Integer from, Integer size) {
        Page<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepository.findAll(PageRequest.of(from / size, size));
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, PageRequest.of(from / size, size));
        }
        return compilations.map(compilationMapper::toCompilationDto).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getById(Long compilationId) {
        Compilation compilation = getCompilationById(compilationId);
        return compilationMapper.toCompilationDto(compilation);
    }

    @Override
    @Transactional
    public CompilationDto add(NewCompilationDto newCompilationDto) {
        Set<Event> events = fetchEvents(newCompilationDto.getEvents());
        Compilation compilation = compilationMapper.toCompilation(newCompilationDto, events);
        Compilation savedCompilation = compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(savedCompilation);
    }

    @Override
    @Transactional
    public CompilationDto update(Long compilationId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = getCompilationById(compilationId);
        Set<Long> eventsIds = updateCompilationRequest.getEvents();
        if (!eventsIds.isEmpty()) {
            Set<Event> updatedEvents = fetchEvents(eventsIds);
            compilation.setEvents(updatedEvents);
        }
        if (Objects.nonNull(updateCompilationRequest.getPinned())) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        String title = updateCompilationRequest.getTitle();
        if (title != null && title.isBlank()) {
            if (compilationRepository.existsByTitleAndIdNot(title, compilation.getId())) {
                throw new ConflictException("Compilation title already exists");
            }
            compilation.setTitle(title);
        }
        Compilation updatedCompilation = compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(updatedCompilation);
    }

    @Override
    @Transactional
    public void delete(Long compilationId) {
        Compilation compilation = getCompilationById(compilationId);
        compilationRepository.delete(compilation);
    }

    private Compilation getCompilationById(Long compilationId) {
        return compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Compilation not found."));
    }

    private Set<Event> fetchEvents(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(eventRepository.findAllById(eventIds));
    }
}