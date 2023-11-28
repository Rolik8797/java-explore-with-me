package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictNameAndEmailException;
import ru.practicum.event.FindObjectInService;
import ru.practicum.user.dto.NewUserDto;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserAdminServiceImp implements UserAdminService {

    private final UserRepository userRepository;
    private final FindObjectInService findObjectInService;


    @Override
    public List<UserDto> get(List<Long> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from, size);
        if (ids == null) {
            log.info("Получен запрос на получение списка пользователей без id");
            return userRepository.findAll(pageable).stream()
                    .map(UserMapper::userToDto)
                    .collect(Collectors.toList());
        } else {
            log.info("Получен запрос на получение списка пользователей по id");
            return userRepository.findByIdIn(ids, pageable).stream()
                    .map(UserMapper::userToDto)
                    .collect(Collectors.toList());
        }
    }


    @Override
    public UserDto create(NewUserDto newUserDto) {
        User user = UserMapper.newUserRequestToUser(newUserDto);
        try {
            log.info("Получен запрос на добавление пользователя {}", newUserDto);
            return UserMapper.userToDto(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new ConflictNameAndEmailException("E-mail: " + newUserDto.getEmail() + " или name пользователя: " +
                    newUserDto.getName() + " уже есть в базе");
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Запрос на добавление пользователя" + newUserDto + " составлен неправильно");
        }
    }

    @Override
    public void delete(Long id) {
        User user = findObjectInService.getUserById(id);
        log.info("Получен запрос на удаление пользователя с id: {}", id);
        userRepository.delete(user);
    }
}