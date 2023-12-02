package ru.practicum.compilation.dto;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Value
@Builder
public class NewCompilationDto {
    List<Long> events;
    boolean pinned;
    @Size(max = 50, message = "Максимальное кол-во символов для описания: 50")
    @NotBlank(message = "title не может быть пустым")
    String title;
}