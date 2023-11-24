package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.category.CategoryService;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.compilation.CompilationService;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.event.EventService;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PublicController {

    private final EventService eventService;

    private final CategoryService categoryService;

    private final CompilationService compilationService;

    @GetMapping("/events")
    public List<EventShortDto> getEvents(@RequestParam(value = "text", required = false) String text,
                                         @RequestParam(value = "categories", required = false) Integer[] categories,
                                         @RequestParam(value = "paid", required = false) Boolean paid,
                                         @RequestParam(value = "rangeStart", required = false) String rangeStart,
                                         @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
                                         @RequestParam(value = "onlyAvailable", required = false,
                                                 defaultValue = "false") Boolean onlyAvailable,
                                         @RequestParam(value = "sort", required = false) String sort,
                                         @RequestParam(value = "from", required = false,
                                                 defaultValue = "0") Integer from,
                                         @RequestParam(value = "size", required = false,
                                                 defaultValue = "10") Integer size,
                                         HttpServletRequest request) {

        log.info("Публичное получение событий с ограничениями: text {}, categories {}, paid {}, rangeStart {}," +
                        " rangeEnd {}, onlyAvailable {}, sort {}, from {}, size {}, request {}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);
        return eventService.getPublicly(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort,
                from, size, request);
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto geyEventById(@PathVariable Integer eventId, HttpServletRequest request) {

        log.info("Публичное получение события с id {}", eventId);
        return eventService.getPubliclyById(eventId, request);
    }

    @GetMapping("/categories")
    public List<CategoryDto> getCategories(@RequestParam(value = "from", required = false, defaultValue = "0")
                                                   Integer from,
                                           @RequestParam(value = "size", required = false, defaultValue = "10")
                                                   Integer size) {

        log.info("Публичное получение всех категорий с ограничениями from {} и size {}", from, size);
        return categoryService.get(from, size);
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto getCategoryById(@PathVariable Integer catId) {

        log.info("Публичное получение категории с id {}", catId);
        return categoryService.getById(catId);
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDto getCompilationById(@PathVariable Integer compId) {

        log.info("Публичное получение подборки событий с id {}", compId);
        return compilationService.getById(compId);
    }

    @GetMapping("/compilations")
    public List<CompilationDto> getCompilations(@RequestParam(value = "pinned", required = false) Boolean pinned,
                                                @RequestParam(value = "from", required = false, defaultValue = "0")
                                                        Integer from,
                                                @RequestParam(value = "size", required = false, defaultValue = "10")
                                                        Integer size) {

        log.info("Публичное получение всех событий ограничениями: pinned {}, from {}, size {}", pinned, from, size);
        return compilationService.get(pinned, from, size);
    }
}