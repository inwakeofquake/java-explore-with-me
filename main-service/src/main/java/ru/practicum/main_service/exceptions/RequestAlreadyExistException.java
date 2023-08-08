package ru.practicum.main_service.exceptions;

public class RequestAlreadyExistException extends RuntimeException {
    public RequestAlreadyExistException(String message) {
        super(message);
    }
}
