package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.CategoryPublicService;
import ru.practicum.category.dto.CategoryDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/categories")
public class CategoryPublicController {

    private final CategoryPublicService categoryPublicService;

    @GetMapping()
    public List<CategoryDto> get(@PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                 @Positive @RequestParam(defaultValue = "10") Integer size) {
        return categoryPublicService.get(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto get(@PathVariable Long catId) {
        return categoryPublicService.get(catId);
    }
}