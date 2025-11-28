package org.thepaddlers.api.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.thepaddlers.api.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        ErrorResponse er = ErrorResponse.of(HttpStatus.BAD_REQUEST, "Malformed JSON request", req.getRequestURI(), "API_INVALID_JSON");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(er);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ErrorResponse er = ErrorResponse.of(HttpStatus.UNPROCESSABLE_ENTITY, "Validation failed", req.getRequestURI(), "API_VALIDATION_ERROR");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(er);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        // ResponseStatusException#getStatusCode returns HttpStatusCode (Spring 6); resolve to HttpStatus safely
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse er = ErrorResponse.of(status, ex.getReason() != null ? ex.getReason() : ex.getMessage(), req.getRequestURI(), "API_ERROR");
        return ResponseEntity.status(status).body(er);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        ErrorResponse er = ErrorResponse.of(HttpStatus.FORBIDDEN, "Access denied", req.getRequestURI(), "AUTH_FORBIDDEN");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(er);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest req) {
        ErrorResponse er = ErrorResponse.of(HttpStatus.UNAUTHORIZED, "Authentication failed", req.getRequestURI(), "AUTH_INVALID_CREDENTIALS");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(er);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        ex.printStackTrace();
        ErrorResponse er = ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", req.getRequestURI(), "SERVER_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(er);
    }
}
