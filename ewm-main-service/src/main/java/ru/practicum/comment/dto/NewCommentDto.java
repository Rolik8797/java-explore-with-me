package ru.practicum.comment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor(force = true)
public final class NewCommentDto {
    @NotBlank
    @Size(min = 20, max = 2000)
    private final String text;
}