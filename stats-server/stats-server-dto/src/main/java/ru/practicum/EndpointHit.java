package ru.practicum;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EndpointHit {
    Integer id;


    @NotBlank(message = "Необходимо указать URI")
    String uri;

    @NotBlank(message = "Необходимо указать IP-адрес пользователя")
    String ip;

    @NotBlank(message = "Необходимо указать дату и время запроса")
    String timestamp;

    @NotBlank(message = "Необходимо указать название приложения")
    String appName;


}