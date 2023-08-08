package ru.practicum.main_service.exceptions;

public class EventIsNotPublishedException extends RuntimeException {
    public EventIsNotPublishedException(String message) {
        super(message);
    }
}
