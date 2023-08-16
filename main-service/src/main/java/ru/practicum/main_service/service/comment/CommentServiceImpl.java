package ru.practicum.main_service.service.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main_service.dto.comment.CommentDto;
import ru.practicum.main_service.dto.comment.NewCommentDto;
import ru.practicum.main_service.entity.Comment;
import ru.practicum.main_service.entity.Event;
import ru.practicum.main_service.entity.User;
import ru.practicum.main_service.exceptions.BadRequestException;
import ru.practicum.main_service.exceptions.ConflictException;
import ru.practicum.main_service.exceptions.NotFoundException;
import ru.practicum.main_service.mapper.CommentMapper;
import ru.practicum.main_service.repository.CommentsRepository;
import ru.practicum.main_service.repository.EventRepository;
import ru.practicum.main_service.repository.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.main_service.utility.Constants.COMMENT_UPDATED;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentsRepository commentsRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public CommentDto createComment(NewCommentDto newCommentDto, Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Error creating comment: user with id={} not found", userId);
                    return new NotFoundException(String.format(
                            "Can't create comment, user with id=%s not found", userId));
                });
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Error creating comment: event with id={} not found", eventId);
                    return new NotFoundException(String.format(
                            "Can't create comment, event with id=%s not found", eventId));
                });

        Comment comment = new Comment();
        comment.setAuthor(user);
        comment.setEvent(event);
        comment.setCreated(LocalDateTime.now());
        comment.setText(newCommentDto.getText());
        Comment savedComment = commentsRepository.save(comment);

        log.info("Comment successfully created by user with ID={} for event with ID={}", userId, eventId);

        return commentMapper.toCommentDto(savedComment);
    }

    @Override
    @Transactional
    public CommentDto updateCommentByUser(NewCommentDto newCommentDto, Long userId, Long commentId) {
        Comment oldComment = commentsRepository.findById(commentId).orElseThrow(() -> {
            log.error("Error updating comment: comment with id={} doesn't exist", commentId);
            return new NotFoundException("Can't update comment, comment doesn't exist");
        });

        if (!userRepository.existsById(userId)) {
            log.error("Error updating comment: user with id={} not found", userId);
            throw new NotFoundException("Can't update comment, user not found");
        }
        if (!oldComment.getAuthor().getId().equals(userId)) {
            log.error("Error updating comment: user with id={} is not the owner of the comment with id={}", userId, commentId);
            throw new ConflictException("Can't update comment, must be owner");
        }
        oldComment.setText(newCommentDto.getText());
        Comment savedComment = commentsRepository.save(oldComment);

        log.info("Comment with ID={} successfully updated by user with ID={}", commentId, userId);

        return commentMapper.toCommentDto(savedComment);
    }

    @Override
    @Transactional
    public CommentDto updateCommentByAdmin(NewCommentDto newCommentDto, Long commentId) {
        Comment oldComment = commentsRepository.findById(commentId).orElseThrow(() -> new NotFoundException(
                "Can't update comment, comment not found"));
        oldComment.setText(newCommentDto.getText());
        Comment savedComment = commentsRepository.save(oldComment);
        log.info(COMMENT_UPDATED, commentId);
        return commentMapper.toCommentDto(savedComment);
    }

    @Override
    public CommentDto getCommentsByIdByUser(Long userId, Long commentId) {
        Comment comment = commentsRepository.findById(commentId).orElseThrow(() -> {
            log.error("Error getting comment");
            return new NotFoundException("Can't get comment");
        });

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Can't get comment, user not found");
        }

        if (!userId.equals(comment.getAuthor().getId())) {
            throw new ConflictException("Can't get comment, must be owner");
        }
        log.info("Get comment with ID = {}", commentId);
        return commentMapper.toCommentDto(comment);
    }

    @Override
    public List<CommentDto> getUserCommentsByCreateTime(
            Long userId,
            LocalDateTime createStart,
            LocalDateTime createEnd,
            Integer from,
            Integer size) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Comment> query = builder.createQuery(Comment.class);

        Root<Comment> root = query.from(Comment.class);

        if (createStart != null && createEnd != null && createEnd.isBefore(createStart)) {
            log.error("Error fetching comments: Comment createEnd must be after createStart");
            throw new BadRequestException("Comment createEnd must be after createStart");
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
        log.info("Get list of comments for user with ID {}", userId);
        return commentMapper.toCommentDtos(comments);
    }

    @Override
    @Transactional
    public void deleteCommentByUser(Long userId, Long commentId) {
        Comment comment = commentsRepository.findById(commentId).orElseThrow(() -> {
            log.error("Error deleting comment: comment doesn't exist or owner mismatch");
            return new NotFoundException("Can't delete comment, if his owner another user or user/comment doesn't exist");
        });
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Can't delete comment, if user doesn't exist");
        }

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Can't delete comment, if his owner another user");
        }
        log.info("Comment with ID = {} was delete", commentId);
        commentsRepository.delete(comment);
    }

    @Override
    public List<CommentDto> getCommentsByEventIdByAdmin(Long eventId, Integer from, Integer size) {
        if (!eventRepository.existsById(eventId)) {
            log.error("Error fetching comments by event id: event with id={} doesn't exist", eventId);
            throw new NotFoundException(String.format("Can't receive comment by id, event with id=%s doesn't exist", eventId));
        }

        Pageable page = PageRequest.of(from / size, size);
        List<Comment> eventComments = commentsRepository.findAllByEventId(eventId, page);

        log.info("Retrieved comment list for event with ID = {}", eventId);

        return commentMapper.toCommentDtos(eventComments);
    }


    @Override
    public CommentDto getCommentsByIdByAdmin(Long commentId) {
        Comment comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("Error fetching comment by id: comment with id={} doesn't exist", commentId);
                    return new NotFoundException(String.format("Can't receive comment by id, the comment with id=%s doesn't exist", commentId));
                });

        log.info("Comment with ID = {} was found", commentId);
        return commentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        if (!commentsRepository.existsById(commentId)) {
            log.error("Error deleting comment: comment with id={} doesn't exist", commentId);
            throw new NotFoundException(String.format("Can't delete comment, comment with id=%s doesn't exist", commentId));
        }

        commentsRepository.deleteById(commentId);

        log.info("Comment with ID = {} was deleted", commentId);
    }

}