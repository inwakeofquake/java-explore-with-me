package ru.practicum.main_service.exceptions;

public abstract class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message);
    }
}
