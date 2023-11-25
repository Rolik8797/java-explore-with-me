package ru.practicum.controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;
import ru.practicum.categories.CategoriesService;

import javax.validation.Valid;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoriesController {

    private final CategoriesService categoriesService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategories(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        return categoriesService.createCategories(newCategoryDto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategories(@PathVariable Long catId) {
        categoriesService.deleteCategories(catId);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategories(
            @PathVariable Long catId,
            @RequestBody @Valid CategoryDto categoryDto
    ) {
        categoryDto.setId(catId);
        return categoriesService.updateCategories(categoryDto);
    }
}