package ru.practicum.user.dto;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Builder
public final class UserDto {
    private final Long id;
    @NotBlank
    private final String name;
    @NotBlank
    @Email
    private final String email;
}