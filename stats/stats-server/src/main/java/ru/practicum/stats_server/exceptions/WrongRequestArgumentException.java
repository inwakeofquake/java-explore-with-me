package ru.practicum.stats_server.exceptions;

public class WrongRequestArgumentException extends RuntimeException {
    public WrongRequestArgumentException(String message) {
        super(message);
    }
}
