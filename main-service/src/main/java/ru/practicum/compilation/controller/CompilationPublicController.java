package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.CompilationPublicService;
import ru.practicum.compilation.dto.CompilationDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/compilations")
public class CompilationPublicController {

    private final CompilationPublicService compilationPublicService;

    @GetMapping()
    public List<CompilationDto> get(@RequestParam(required = false) Boolean pinned,
                                    @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                    @Positive @RequestParam(defaultValue = "10") Integer size) {
        return compilationPublicService.get(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto get(@PathVariable Long compId) {
        return compilationPublicService.get(compId);
    }
}