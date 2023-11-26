package ru.practicum.user.dto;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Value
@Builder
public class NewUserDto {
    @Email
    @NotBlank(message = "Поле name не должно быть пустым")
    @Size(min = 6, max = 254, message = "Ограничение по символам в поле e-mail: min = 6, max = 254")
    String email;
    @Size(min = 2, max = 250, message = "Ограничение по символам в поле name: min = 2, max = 250")
    @NotBlank(message = "Поле name не должно быть пустым")
    String name;
}