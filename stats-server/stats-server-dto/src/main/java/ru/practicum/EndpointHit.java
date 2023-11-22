package ru.practicum;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;

@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EndpointHit {
    @Getter
    @Setter
    Integer id;

    @NotBlank(message = "Необходимо указать идентификатор сервиса")
    @Getter
    @Setter
    String app;

    @NotBlank(message = "Необходимо указать URI")
    @Getter
    @Setter
    String uri;

    @NotBlank(message = "Необходимо указать IP-адрес пользователя")
    @Getter
    @Setter
    String ip;

    @NotBlank(message = "Необходимо указать дату и время запроса")
    @Getter
    @Setter
    String timestamp;
}