package ru.practicum.main_service.exceptions;

public class CommentConflictException extends RuntimeException {
    public CommentConflictException(String message) {
        super(message);
    }
}
