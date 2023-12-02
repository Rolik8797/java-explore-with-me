package ru.practicum.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ResourceNotFoundException;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FindObjectInService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CompilationRepository compilationRepository;
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;


    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Категория c id: " + id + " не найдена"));
    }


    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Пользователь c id: " + id + " не найден"));
    }


    public Compilation getCompilationById(Long id) {
        return compilationRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Подборка событий c id = " + id + " не найдена"));
    }


    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Событие c id = " + id + " не найдено"));
    }

    public Request getRequestById(Long id) {
        return requestRepository.findById(id).orElseThrow(()
                -> new ResourceNotFoundException("Запрос на участие c id = " + id + " не найден"));
    }


    public boolean isRelatedEvent(Category category) {
        List<Event> findEventByCategory = eventRepository.findEventByCategoryIs(category);
        return !findEventByCategory.isEmpty();
    }
}