package com.codefortress.shared.api;

import com.codefortress.identity.registration.EmailAlreadyRegisteredException;
import com.codefortress.identity.authentication.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleDuplicatedEmail(
            EmailAlreadyRegisteredException exception
    ) {
        return new ApiErrorResponse(
                "EMAIL_ALREADY_REGISTERED",
                exception.getMessage(),
                Map.of(),
                Instant.now()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidation(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> fieldErrors = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        this::getErrorMessage,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));

        return new ApiErrorResponse(
                "VALIDATION_ERROR",
                "Request validation failed",
                fieldErrors,
                Instant.now()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleIllegalArgument(
            IllegalArgumentException exception
    ) {
        return new ApiErrorResponse(
                "INVALID_ARGUMENT",
                exception.getMessage(),
                Map.of(),
                Instant.now()
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {
        return new ApiErrorResponse(
                "INVALID_CREDENTIALS",
                exception.getMessage(),
                Map.of(),
                Instant.now()
        );
    }

    private String getErrorMessage(FieldError fieldError) {
        String message = fieldError.getDefaultMessage();
        return message == null ? "invalid value" : message;
    }
}