package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    @Transactional
    public UserDto add(NewUserRequest request) {
        repository.findFirst1ByName(request.getName()).ifPresent((user) -> {
            throw new ConflictException("User name already exists.");
        });
        User newUser = mapper.toUser(request);
        User savedUser = repository.save(newUser);
        return mapper.toUserDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll(@Nullable List<Long> ids, Integer from, Integer size) {
        if (Objects.nonNull(ids)) {
            List<User> users = repository.findAllById(ids);
            return users.stream().map(mapper::toUserDto).collect(Collectors.toList());
        } else {
            int page = from / size;
            Page<User> users = repository.findAll(PageRequest.of(page, size));
            return users.map(mapper::toUserDto).getContent();
        }
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));
        repository.delete(user);
    }
}