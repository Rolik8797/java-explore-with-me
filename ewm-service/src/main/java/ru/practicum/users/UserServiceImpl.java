package ru.practicum.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ObjectNotFoundException;
import ru.practicum.users.dto.NewUserRequestDto;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.mapper.UserMapper;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        if (from < 0 || size <= 0) {
            return Collections.emptyList();
        }
        int offset = from > 0 ? from / size : 0;
        PageRequest page = PageRequest.of(offset, size);
        List<User> users;
        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAll(page).getContent();
        } else {
            users = userRepository.findByIdIn(ids, page);
        }
        log.info("GET request to search for users, with id: {}", ids);

        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto createUser(NewUserRequestDto userRequestDto) {
        if (userRepository.existsUserByName(userRequestDto.getName())) {
            throw new ConflictException("There is already such a user");
        }
        User user = UserMapper.toUser(userRequestDto);
        log.info("POST request to save user: {}", user.getName());
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long userId) {
        if (!isUserExists(userId)) {
            throw new ObjectNotFoundException("User does not exist");
        }
        log.info("DELETE request to delete a user: c id: {}", userId);
        userRepository.deleteById(userId);
    }

    public boolean isUserExists(Long userId) {
        var userOptional = userRepository.findById(userId);
        return userOptional.isPresent();
    }
}