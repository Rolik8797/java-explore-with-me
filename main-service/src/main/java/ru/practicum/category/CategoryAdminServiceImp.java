package ru.practicum.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.FindObjectInService;
import ru.practicum.exception.ConflictDeleteException;
import ru.practicum.exception.ConflictNameCategoryException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryAdminServiceImp implements CategoryAdminService {

    private final CategoryRepository categoryRepository;
    private final FindObjectInService findObjectInService;

    @Override
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        Category category = CategoryMapper.newCategoryDtoToCategory(newCategoryDto);
        log.info("Получен запрос на добавление категории с названием: {}", newCategoryDto.getName());
        checkNameCategory(category.getName());
        return CategoryMapper.categoryToCategoryDto(categoryRepository.save(category));
    }

    @Override
    public CategoryDto update(Long id, CategoryDto categoryDto) {
        Category category = findObjectInService.getCategoryById(id);
        if (!categoryDto.getName().equals(category.getName())) {
            checkNameCategory(categoryDto.getName());
            category.setName(categoryDto.getName());
        }
        log.info("Получен запрос на обновлении категории c id: {}", id);
        return CategoryMapper.categoryToCategoryDto(categoryRepository.save(category));
    }

    @Override
    public void delete(Long id) {
        Category category = findObjectInService.getCategoryById(id);
        if (findObjectInService.isRelatedEvent(category)) {
            throw new ConflictDeleteException("Существуют события, связанные с категорией " + category.getName());
        }
        log.info("Получен запрос на удаление категории c id: {}", id);
        categoryRepository.delete(category);
    }

    private void checkNameCategory(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new ConflictNameCategoryException("Имя категории " + name + " уже есть в базе");
        }
    }
}