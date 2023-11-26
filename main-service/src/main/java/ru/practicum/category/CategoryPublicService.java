package ru.practicum.category;

import ru.practicum.category.dto.CategoryDto;

import java.util.List;

public interface CategoryPublicService {

    List<CategoryDto> get(int from, int size);

    CategoryDto get(Long catId);
}