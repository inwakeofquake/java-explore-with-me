package ru.practicum.main_service.exceptions;

public class CommentNotExistException extends RuntimeException {
    public CommentNotExistException(String message) {
        super(message);
    }
}
