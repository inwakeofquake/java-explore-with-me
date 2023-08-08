package ru.practicum.main_service.exceptions;

public class CompilationNotExistException extends RuntimeException {
    public CompilationNotExistException(String message) {
        super(message);
    }
}
