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
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Can't create comment, user with id=%s doesn't exist", userId)));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format(
                        "Can't create comment, event with id=%s doesn't exist", eventId)));

        Comment comment = new Comment();
        comment.setAuthor(user);
        comment.setEvent(event);
        comment.setCreated(LocalDateTime.now());
        comment.setText(newCommentDto.getText());
        return commentMapper.toCommentDto(commentsRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto updateCommentByUser(NewCommentDto newCommentDto, Long userId, Long commentId) {
        Comment oldComment = commentsRepository.findById(commentId).orElseThrow(() -> new NotFoundException(
                "Can't update comment, comment doesn't exist"));
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Can't delete comment, if user doesn't exist");
        }
        if (!oldComment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Can't delete comment, if his owner another user");
        }
        oldComment.setText(newCommentDto.getText());
        Comment savedComment = commentsRepository.save(oldComment);
        log.debug(COMMENT_UPDATED, commentId);
        return commentMapper.toCommentDto(savedComment);
    }

    @Override
    @Transactional
    public CommentDto updateCommentByAdmin(NewCommentDto newCommentDto, Long commentId) {
        Comment oldComment = commentsRepository.findById(commentId).orElseThrow(() -> new NotFoundException(
                "Can't update comment, comment doesn't exist"));
        oldComment.setText(newCommentDto.getText());
        Comment savedComment = commentsRepository.save(oldComment);
        log.debug(COMMENT_UPDATED, commentId);
        return commentMapper.toCommentDto(savedComment);
    }

    @Override
    public CommentDto getCommentsByIdByUser(Long userId, Long commentId) {
        Comment comment = commentsRepository.findById(commentId).orElseThrow(() -> new NotFoundException(
                "Can't get comment"));
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Can't get comment, if user doesn't exist");
        }

        if (!userId.equals(comment.getAuthor().getId())) {
            throw new ConflictException("Can't get comment created by another user");
        }
        log.debug("Get comment with ID = {}", commentId);
        return commentMapper.toCommentDto(comment);
    }

    @Override
    public List<CommentDto> getUserCommentsByCreateTime(
            Long userId,
            LocalDateTime createStart,
            LocalDateTime createEnd,
            Integer from,
            Integer size) {
        userRepository.findById(userId);

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Comment> query = builder.createQuery(Comment.class);

        Root<Comment> root = query.from(Comment.class);

        if (createStart != null && createEnd != null && createEnd.isBefore(createStart)) {
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
        log.debug("Get comment`s list of user with ID = {}", userId);
        return commentMapper.toCommentDtos(comments);
    }

    @Override
    @Transactional
    public void deleteCommentByUser(Long userId, Long commentId) {
        Comment comment = commentsRepository.findById(commentId).orElseThrow(() -> new NotFoundException(
                "Can't delete comment, if his owner another user or user/comment doesn't exist"));
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Can't delete comment, if user doesn't exist");
        }

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Can't delete comment, if his owner another user");
        }
        log.debug("Comment with ID = {} was delete", commentId);
        commentsRepository.delete(comment);
    }

    @Override
    public List<CommentDto> getCommentsByEventIdByAdmin(Long eventId, Integer from, Integer size) {
        eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(String.format(
                "Can't receive comment by id, event with id=%s doesn't exist", eventId)));
        Pageable page = PageRequest.of(from / size, size);
        List<Comment> eventComments = commentsRepository.findAllByEventId(eventId, page);
        log.debug("Get comment`s list of event with ID = {}", eventId);
        return commentMapper.toCommentDtos(eventComments);
    }

    @Override
    public CommentDto getCommentsByIdByAdmin(Long commentId) {
        Comment comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Can't receive comment by id, the comment with id=%s doesn't exist", commentId)));
        log.debug("Comment with ID = {} was found", commentId);
        return commentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        commentsRepository.deleteById(commentId);
        log.debug("Comment with ID = {} was delete", commentId);
    }
}