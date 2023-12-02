package ru.practicum.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.FindObjectInService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryPublicServiceImp implements CategoryPublicService {

    private final CategoryRepository categoryRepository;
    private final FindObjectInService findObjectInService;

    @Override
    public List<CategoryDto> get(int from, int size) {
        log.info("Получен запрос на список всех категорий");
        return categoryRepository.findAll(PageRequest.of(from, size)).stream()
                .map(CategoryMapper::categoryToCategoryDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDto get(Long id) {
        Category category = findObjectInService.getCategoryById(id);
        log.info("Получен запрос на поиск категории по id: {}", id);
        return CategoryMapper.categoryToCategoryDto(category);
    }
}