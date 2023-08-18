package ru.practicum.main_service.exceptions;

public class ConflictException extends CustomException {
    public ConflictException(String message) {
        super(message);
    }
}
