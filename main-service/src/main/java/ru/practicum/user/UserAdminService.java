package ru.practicum.user;


import ru.practicum.user.dto.NewUserDto;
import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserAdminService {

    List<UserDto> get(List<Long> ids, int from, int size);

    UserDto create(NewUserDto newUserDto);

    void delete(Long id);
}