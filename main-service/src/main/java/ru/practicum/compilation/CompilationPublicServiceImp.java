package ru.practicum.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.FindObjectInService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationPublicServiceImp implements CompilationPublicService {

    private final CompilationRepository compilationRepository;
    private final FindObjectInService findObjectInService;


    @Override
    public List<CompilationDto> get(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from, size);
        log.info("Получен запрос на поиск всех подборок событий");
        return compilationRepository.findAllByPinnedIs(pinned, pageable).stream()
                .map(CompilationMapper::compilationToCompilationDto).collect(Collectors.toList());
    }

    @Override
    public CompilationDto get(Long id) {
        log.info("Получен запрос на поиск подборки событий по id: {}", id);
        return CompilationMapper.compilationToCompilationDto(findObjectInService.getCompilationById(id));
    }
}