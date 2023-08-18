package ru.practicum.main_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.main_service.entity.ApiError;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static ru.practicum.main_service.utility.Constants.DATE;

@RestControllerAdvice
public class ErrorHandler {
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE);

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ApiError handleConflictException(final ConflictException exception) {
        return new ApiError(exception.getMessage(), "CONFLICT exception is thrown",
                HttpStatus.CONFLICT.getReasonPhrase().toUpperCase(), LocalDateTime.now().format(dateFormatter));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiError handleBadRequestException(final BadRequestException exception) {
        return new ApiError(exception.getMessage(), "BAD REQUEST exception is thrown",
                HttpStatus.BAD_REQUEST.getReasonPhrase().toUpperCase(), LocalDateTime.now().format(dateFormatter));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ApiError handleNotFoundException(final NotFoundException exception) {
        return new ApiError(exception.getMessage(), "NOT FOUND exception is thrown",
                HttpStatus.NOT_FOUND.getReasonPhrase().toUpperCase(), LocalDateTime.now().format(dateFormatter));
    }

}
