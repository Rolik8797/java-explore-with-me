package ru.practicum.category;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public List<CategoryDto> getAll(Integer from, Integer size) {
        Page<Category> categoryPage = categoryRepository.findAll(PageRequest.of(from, size));
        return categoryPage.map(categoryMapper::toCategoryDto).getContent();
    }

    @Override
    @Transactional
    public CategoryDto getById(Long categoryId) {
        Category category = getCategoryIfExists(categoryId);
        return categoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public CategoryDto add(NewCategoryDto newCategoryDto) {
        Category category = categoryMapper.toCategory(newCategoryDto);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toCategoryDto(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDto update(CategoryDto categoryDto, Long categoryId) {
        Category category = getCategoryIfExists(categoryId);
        String newName = categoryDto.getName();
        String existingName = category.getName();
        category.setName(StringUtils.defaultIfBlank(newName, existingName));
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long categoryId) {
        Category category = getCategoryIfExists(categoryId);
        if (eventRepository.findByCategoryId(categoryId).isPresent()) {
            throw new ConflictException("Category could not been deleted.");
        }
        categoryRepository.delete(category);
    }

    private Category getCategoryIfExists(long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found."));
    }
}