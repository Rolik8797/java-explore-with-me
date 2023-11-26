package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.CompilationAdminService;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.request.dto.UpdateCompilationRequest;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/compilations")
public class CompilationAdminController {
    private final CompilationAdminService compilationAdminService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(@Validated @RequestBody NewCompilationDto newCompilationDto) {
        return compilationAdminService.create(newCompilationDto);
    }


    @PatchMapping("/{compId}")
    public CompilationDto update(@PathVariable Long compId,
                                 @Validated @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        return compilationAdminService.update(compId, updateCompilationRequest);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long compId) {
        compilationAdminService.delete(compId);
    }
}