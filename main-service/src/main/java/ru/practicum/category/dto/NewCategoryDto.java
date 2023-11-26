package ru.practicum.category.dto;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewCategoryDto {
    @Size(max = 50, message = "Максимальное кол-во символов для поля name: 50")
    @NotBlank(message = "Поле name не должно быть пустым")
    private String name;
}