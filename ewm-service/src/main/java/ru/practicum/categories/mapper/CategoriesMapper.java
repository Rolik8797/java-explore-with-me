package ru.practicum.categories.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;
import ru.practicum.categories.model.Categories;


@UtilityClass
public class CategoriesMapper {

    public static CategoryDto toCategoryDto(Categories categories) {
        if (categories == null) {
            return CategoryDto.builder()
                    .id(null)
                    .name(null)
                    .build();
        }
        return CategoryDto.builder()
                .id(Long.valueOf(categories.getId()))
                .name(categories.getName())
                .build();
    }

    public static Categories toCategories(NewCategoryDto newCategoryDto) {
        if (newCategoryDto == null) {
            return Categories.builder()
                    .name(null)
                    .build();
        }
        return Categories.builder()
                .name(newCategoryDto.getName())
                .build();
    }
}