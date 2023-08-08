package ru.practicum.main_service.exceptions;

public class RequestNotExistException extends RuntimeException {
    public RequestNotExistException(String message) {
        super(message);
    }
}
