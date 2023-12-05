package ru.practicum.comment;


import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentService {
    CommentDto add(NewCommentDto newCommentDto, Long userId, Long eventId);

    CommentDto update(NewCommentDto newCommentDto, Long userId, Long commentId);

    void delete(Long userId, Long commentId);

    CommentDto getByCommentId(Long userId, Long commentId);

    List<CommentDto> getAllByCreateTime(Long userId, LocalDateTime createStart, LocalDateTime createEnd, Integer from, Integer size);

    CommentDto updateByAdmin(NewCommentDto newCommentDto, Long commentId);

    void deleteByAdmin(Long commentId);

    CommentDto getByCommentIdByAdmin(Long commentId);

    List<CommentDto> getByEventIdByAdmin(Long eventId, Integer from, Integer size);
}