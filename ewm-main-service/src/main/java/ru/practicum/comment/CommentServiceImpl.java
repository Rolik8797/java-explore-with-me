package ru.practicum.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public CommentDto add(NewCommentDto newCommentDto, Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found."));
        Event event = getEventIfExists(eventId);
        Comment comment = Comment.builder()
                .text(newCommentDto.getText())
                .author(user)
                .event(event)
                .created(LocalDateTime.now())
                .build();
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto update(NewCommentDto newCommentDto, Long userId, Long commentId) {
        Comment oldComment = getCommentIfExists(commentId);
        checkUser(userId);
        if (!oldComment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Comment cannot be updated: user is not the author of the post");
        }
        Comment newComment = Comment.builder()
                .id(oldComment.getId())
                .text(newCommentDto.getText())
                .author(oldComment.getAuthor())
                .event(oldComment.getEvent())
                .created(oldComment.getCreated())
                .build();
        return commentMapper.toCommentDto(commentRepository.save(newComment));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long commentId) {
        Comment comment = getCommentIfExists(commentId);
        checkUser(userId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Comment cannot be deleted: user is not the author of the post");
        }
        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getByCommentId(Long userId, Long commentId) {
        Comment comment = getCommentIfExists(commentId);
        checkUser(userId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Can't get comment created by another user");
        }

        return commentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllByCreateTime(Long userId, LocalDateTime createStart, LocalDateTime createEnd,
                                               Integer from, Integer size) {
        checkUser(userId);
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Comment> query = builder.createQuery(Comment.class);
        Root<Comment> root = query.from(Comment.class);
        if (createStart != null && createEnd != null) {
            if (createEnd.isBefore(createStart)) {
                throw new ConflictException("CreateEnd must be after createStart");
            }
        }
        Predicate criteria = root.get("author").in(userId);
        if (createStart != null) {
            Predicate greaterTime = builder.greaterThanOrEqualTo(
                    root.get("created").as(LocalDateTime.class), createStart);
            criteria = builder.and(criteria, greaterTime);
        }
        if (createEnd != null) {
            Predicate lessTime = builder.lessThanOrEqualTo(
                    root.get("created").as(LocalDateTime.class), createEnd);
            criteria = builder.and(criteria, lessTime);
        }
        query.select(root).where(criteria).orderBy(builder.asc(root.get("created")));
        List<Comment> comments = entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultList();
        return commentMapper.toCommentDtoList(comments);
    }

    @Override
    @Transactional
    public CommentDto updateByAdmin(NewCommentDto newCommentDto, Long commentId) {
        Comment oldComment = getCommentIfExists(commentId);
        Comment newComment = Comment.builder()
                .id(oldComment.getId())
                .text(newCommentDto.getText())
                .author(oldComment.getAuthor())
                .event(oldComment.getEvent())
                .created(oldComment.getCreated())
                .build();
        return commentMapper.toCommentDto(commentRepository.save(newComment));
    }

    @Override
    @Transactional
    public void deleteByAdmin(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getByCommentIdByAdmin(Long commentId) {
        Comment comment = getCommentIfExists(commentId);
        return commentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getByEventIdByAdmin(Long eventId, Integer from, Integer size) {
        getEventIfExists(eventId);
        Pageable page = PageRequest.of(from / size, size);
        List<Comment> eventComments = commentRepository.findAllByEvent_Id(eventId, page);
        return commentMapper.toCommentDtoList(eventComments);
    }

    private void checkUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found.");
        }
    }

    private Comment getCommentIfExists(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment not found."));
    }

    private Event getEventIfExists(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found."));
    }
}