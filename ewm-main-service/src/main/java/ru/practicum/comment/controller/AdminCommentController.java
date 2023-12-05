package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.CommentService;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;


@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getByEventId(@RequestParam(value = "eventId") Long eventId,
                                         @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                         @RequestParam(value = "size", defaultValue = "10") @Positive Integer size) {
        return commentService.getByEventIdByAdmin(eventId, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto getByCommentId(@PathVariable(value = "commentId") Long commentId) {
        return commentService.getByCommentIdByAdmin(commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto update(@RequestBody @Valid NewCommentDto newCommentDto,
                             @PathVariable(value = "commentId") Long commentId) {
        return commentService.updateByAdmin(newCommentDto, commentId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(value = "commentId") Long commentId) {
        commentService.deleteByAdmin(commentId);
    }
}