package ru.practicum.stats.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import ru.practicum.common.formatter.DateTimeFormatter;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgumentException(final IllegalArgumentException ex, final WebRequest request) {
        String path = request.getDescription(false).substring(4);
        log.error("[INVALID DATA]: In: {}. Path: {}; Message: {}.", getClassAndMethodName(ex.getStackTrace()), path, ex.getMessage());

        return new ErrorResponse(
                LocalDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                path,
                ex.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingRequestParamException(
            final MissingServletRequestParameterException ex,
            final WebRequest request) {
        String path = request.getDescription(false).substring(4);
        log.error("[REQUEST PRAM ERROR]: In: {}. Path: {}; Message: {}.", getClassAndMethodName(ex.getStackTrace()), path, ex.getMessage());

        return new ErrorResponse(
                LocalDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                path,
                ex.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException ex,
            final WebRequest request) {
        String errors = ex.getBindingResult().getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(" "));
        String path = request.getDescription(false).substring(4);
        log.error("[VALIDATION ERROR]: Path: {}; Message: {}.", path, ex.getMessage());

        return new ErrorResponse(
                LocalDateTime.now().format(DateTimeFormatter.DATE_TIME_FORMATTER),
                HttpStatus.BAD_REQUEST.value(),
                path,
                errors
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable ex, final WebRequest request) {

        String path = request.getDescription(false).substring(4);
        log.error("[INTERNAL SERVER ERROR]: Path: {}: Message: {}", path, ex.getMessage());
        return new ErrorResponse(
                LocalDateTime.now().toString(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                path,
                ex.getMessage()
        );
    }

    private String getClassAndMethodName(StackTraceElement[] stackTrace) {
        if (stackTrace.length > 1) {
            StackTraceElement element = stackTrace[0];
            return element.getClassName() + "." + element.getMethodName();
        } else {
            return "";
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ErrorResponse {

        private String timestamp;

        private int status;

        private String path;

        private String error;
    }
}