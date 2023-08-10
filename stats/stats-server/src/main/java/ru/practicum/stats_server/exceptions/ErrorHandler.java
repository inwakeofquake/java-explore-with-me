package ru.practicum.stats_server.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.stats_server.entity.ApiError;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static ru.practicum.stats_server.utility.Constants.DATE;

@RestControllerAdvice
public class ErrorHandler {
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE);

    @ExceptionHandler
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleWrongRequestArgument(final WrongRequestArgumentException exception) {
        return new ApiError(exception.getMessage(), "Wrong request argument.",
                HttpStatus.BAD_REQUEST.getReasonPhrase().toUpperCase(), LocalDateTime.now().format(dateFormatter));
    }

}
