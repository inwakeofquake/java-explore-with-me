package ru.practicum.main_service.exceptions;

public class WrongRequestArgumentException extends RuntimeException {
    public WrongRequestArgumentException(String message) {
        super(message);
    }
}
