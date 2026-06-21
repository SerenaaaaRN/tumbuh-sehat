package com.nutricare.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class GeminiException extends RuntimeException {
    public GeminiException(String message) { super(message); }
}
