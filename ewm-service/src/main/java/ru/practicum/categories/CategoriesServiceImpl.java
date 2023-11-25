package ru.practicum.categories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;
import ru.practicum.categories.mapper.CategoriesMapper;
import ru.practicum.categories.model.Categories;
import ru.practicum.categories.repository.CategoriesRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ObjectNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.tomcat.util.collections.SynchronizedStack.DEFAULT_SIZE;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriesServiceImpl implements CategoriesService {

    private final CategoriesRepository categoriesRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        int offset = (from != null && from > 0 && size != null) ? from / size : 0;
        PageRequest page = PageRequest.of(offset, size != null ? size : DEFAULT_SIZE);
        List<Categories> categoriesList = categoriesRepository.findAll(page).getContent();
        log.info("GET request to get a list of categories");
        return categoriesList.stream().map(CategoriesMapper::toCategoryDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoriesId(Long catId) {
        Categories categories = getCategoriesIfExist(catId);
        return CategoriesMapper.toCategoryDto(categories);
    }

    @Override
    public CategoryDto createCategories(NewCategoryDto newCategoryDto) {
        if (categoriesRepository.existsCategoriesByName(newCategoryDto.getName())) {
            throw new ConflictException("This category already exists");
        }
        Categories categories = categoriesRepository.save(CategoriesMapper.toCategories(newCategoryDto));
        log.info("POST request to save a category: {}", newCategoryDto.getName());
        return CategoriesMapper.toCategoryDto(categories);
    }

    @Override
    public void deleteCategories(Long catId) {

        var category = categoriesRepository.findById(catId);

        if (eventRepository.existsEventsByCategory_Id(catId)) {
            throw new ConflictException("There is already such a user");
        }
        categoriesRepository.deleteById(catId);
        log.info("Request DELETE to delete category: id: {}", catId);
    }

    @Override
    public CategoryDto updateCategories(CategoryDto categoryDto) {

        Categories categories = getCategoriesIfExist(categoryDto.getId());

        if (categoriesRepository.existsCategoriesByNameAndIdNot(categoryDto.getName(), categoryDto.getId())) {
            throw new ConflictException("This category already exists");
        }

        categories.setName(categoryDto.getName());
        log.info("Request PATH to change category id: {}", categoryDto.getId());
        return CategoriesMapper.toCategoryDto(categoriesRepository.save(categories));
    }

    private Categories getCategoriesIfExist(Long catId) {
        return categoriesRepository.findById(catId).orElseThrow(
                () -> new ObjectNotFoundException("Selected category not found"));
    }
}