package ru.practicum.main_service.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_service.dto.comment.CommentDto;
import ru.practicum.main_service.dto.comment.NewCommentDto;
import ru.practicum.main_service.service.comment.CommentService;

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
    public List<CommentDto> getCommentsByEventId(
            @RequestParam(value = "eventId") @Positive Long eventId,
            @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(value = "size", defaultValue = "10") @Positive Integer size) {
        return commentService.getCommentsByEventIdByAdmin(eventId, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto getCommentById(@PathVariable(value = "commentId") Long commentId) {
        return commentService.getCommentsByIdByAdmin(commentId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable(value = "commentId") Long commentId) {
        commentService.deleteCommentByAdmin(commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@RequestBody @Valid NewCommentDto newCommentDto,
                                    @PathVariable(value = "commentId") Long commentId) {
        return commentService.updateCommentByAdmin(newCommentDto, commentId);
    }
}
