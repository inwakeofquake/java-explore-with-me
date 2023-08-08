package ru.practicum.main_service.exceptions;

public class EventNotExistException extends RuntimeException {
    public EventNotExistException(String message) {
        super(message);
    }
}
