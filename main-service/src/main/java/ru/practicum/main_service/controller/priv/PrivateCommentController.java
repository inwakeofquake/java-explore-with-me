package ru.practicum.main_service.controller.priv;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_service.dto.comment.CommentDto;
import ru.practicum.main_service.dto.comment.NewCommentDto;
import ru.practicum.main_service.service.comment.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.main_service.utility.Constants.DATE;

@RestController
@RequestMapping("/users/{userId}/comments")
@Validated
@RequiredArgsConstructor
public class PrivateCommentController {
    private final CommentService commentService;

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@RequestBody @Valid NewCommentDto newCommentDto,
                                    @PathVariable(value = "userId") Long userId,
                                    @PathVariable(value = "eventId") Long eventId) {
        return commentService.createComment(newCommentDto, userId, eventId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@RequestBody @Valid NewCommentDto newCommentDto,
                                    @PathVariable(value = "userId") Long userId,
                                    @PathVariable(value = "commentId") Long commentId) {
        return commentService.updateCommentByUser(newCommentDto, userId, commentId);
    }

    @GetMapping("/{commentId}")
    public CommentDto getCommentById(@PathVariable(value = "userId") Long userId,
                                     @PathVariable(value = "commentId") Long commentId) {
        return commentService.getCommentsByIdByUser(userId, commentId);
    }

    @GetMapping
    public List<CommentDto> getUserCommentsByCreateTime(
            @PathVariable(value = "userId") Long userId,
            @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(value = "size", defaultValue = "10") @Positive Integer size,
            @RequestParam(value = "createStart", required = false)
            @DateTimeFormat(pattern = DATE) LocalDateTime createStart,
            @RequestParam(value = "createEnd", required = false)
            @DateTimeFormat(pattern = DATE) LocalDateTime createEnd) {
        return commentService.getUserCommentsByCreateTime(userId, createStart, createEnd, from, size);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByUser(@PathVariable(value = "userId") Long userId,
                                    @PathVariable(value = "commentId") Long commentId) {
        commentService.deleteCommentByUser(userId, commentId);
    }
}