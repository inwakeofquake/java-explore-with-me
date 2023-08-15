package ru.practicum.main_service.exceptions;

public class NotFoundException extends CustomException {
    public NotFoundException(String message) {
        super(message);
    }
}
