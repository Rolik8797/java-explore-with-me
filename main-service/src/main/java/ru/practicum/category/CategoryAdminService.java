package ru.practicum.category;


import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;

public interface CategoryAdminService {

    CategoryDto create(NewCategoryDto newCategoryDto);

    CategoryDto update(Long id, CategoryDto newCategoryDto);

    void delete(Long id);
}