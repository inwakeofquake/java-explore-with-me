package ru.practicum.main_service.exceptions;

public class AlreadyPublishedException extends RuntimeException {
    public AlreadyPublishedException(String message) {
        super(message);
    }
}
