package ru.practicum.main_service.exceptions;

public class ParticipantLimitException extends RuntimeException {
    public ParticipantLimitException(String message) {
        super(message);
    }
}
