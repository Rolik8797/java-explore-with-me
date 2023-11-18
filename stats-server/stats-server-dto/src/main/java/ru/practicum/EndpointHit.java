package ru.practicum;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EndpointHit {
    Integer id;

    @NotNull(message = "Необходимо указать идентификатор сервиса")
    @NotBlank(message = "Идентификатор сервиса не может состоять из пустой строки")
    String app;

    @NotNull(message = "Необходимо указать URI")
    @NotBlank(message = "URI не может состоять из пустой строки")
    String uri;

    @NotNull(message = "Необходимо указать IP-адрес пользователя")
    @NotBlank(message = "IP-адрес пользователя не может состоять из пустой строки")
    String ip;

    @NotNull(message = "Необходимо указать дату и время запроса")
    @NotBlank(message = "Дата и время запроса не может состоять из пустой строки")
    String timestamp;
}