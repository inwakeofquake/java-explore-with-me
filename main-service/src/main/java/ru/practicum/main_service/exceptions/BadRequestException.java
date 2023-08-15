package ru.practicum.main_service.exceptions;

public class BadRequestException extends CustomException {
    public BadRequestException(String message) {
        super(message);
    }
}
