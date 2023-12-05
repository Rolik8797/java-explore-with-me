package ru.practicum.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserShortDto {
    private Long id;
    private String name;
}