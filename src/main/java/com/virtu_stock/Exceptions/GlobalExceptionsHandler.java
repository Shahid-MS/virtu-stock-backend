package com.virtu_stock.Exceptions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.virtu_stock.Exceptions.CustomExceptions.BadRequestException;
import com.virtu_stock.Exceptions.CustomExceptions.DuplicateResourceException;
import com.virtu_stock.Exceptions.CustomExceptions.EmailFormattingException;
import com.virtu_stock.Exceptions.CustomExceptions.InvalidEmailAddressException;
import com.virtu_stock.Exceptions.CustomExceptions.InvalidPaginationParameterException;
import com.virtu_stock.Exceptions.CustomExceptions.InvalidRequestException;
import com.virtu_stock.Exceptions.CustomExceptions.InvalidSortFieldException;
import com.virtu_stock.Exceptions.CustomExceptions.MailDeliveryException;
import com.virtu_stock.Exceptions.CustomExceptions.ResourceNotFoundException;
import com.virtu_stock.Exceptions.CustomExceptions.UnauthorizedException;

import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionsHandler {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm:ss a");

    private Map<String, Object> buildErrorResponse(String error, String message, HttpStatus status) {
        return Map.of(
                "timestamp", LocalDateTime.now().format(FORMATTER),
                "status", status.value(),
                "error", error,
                "message", message);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse("BAD_REQUEST", ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildErrorResponse("UNAUTHORIZED", ex.getMessage(), HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse("NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<?> handleDuplicate(DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildErrorResponse("CONFLICT", ex.getMessage(), HttpStatus.CONFLICT));
    }

    @ExceptionHandler(InvalidPaginationParameterException.class)
    public ResponseEntity<?> handleInvalidPagination(InvalidPaginationParameterException ex) {
        return ResponseEntity.badRequest()
                .body(buildErrorResponse("INVALID_PAGINATION", ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(InvalidSortFieldException.class)
    public ResponseEntity<?> handleInvalidSort(InvalidSortFieldException ex) {
        return ResponseEntity.badRequest()
                .body(buildErrorResponse("INVALID_SORT_FIELD", ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<?> handleInvalidRequest(InvalidRequestException ex) {
        return ResponseEntity.badRequest()
                .body(buildErrorResponse("INVALID_REQUEST", ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(
            org.springframework.security.authentication.BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildErrorResponse(
                        "INVALID_CREDENTIALS",
                        "Invalid email or password",
                        HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabled(org.springframework.security.authentication.DisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildErrorResponse(
                        "USER_DISABLED",
                        "User account is disabled",
                        HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFound(
            org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(
                        "USER_NOT_FOUND",
                        "User not found",
                        HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.badRequest().body(
                Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "status", 400,
                        "error", "VALIDATION_ERROR",
                        "message", errors));

    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleEmptyBody(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "timestamp", LocalDateTime.now().format(FORMATTER),
                        "status", 400,
                        "error", "INVALID_REQUEST_BODY",
                        "message", "Request body is missing or malformed"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(buildErrorResponse(
                        "ACCESS_DENIED",
                        ex.getMessage(),
                        HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Logger log = LoggerFactory.getLogger(this.getClass());
        // log full exception for diagnostics (stacktrace)
        log.warn("Data integrity violation", ex);

        // Try to get a useful root-cause message
        Throwable root = ex.getRootCause() != null ? ex.getRootCause() : ex;
        String rootMessage = root.getMessage() != null ? root.getMessage().toLowerCase() : "";

        String code = "DATA_INTEGRITY_VIOLATION";
        String userMessage = "A data integrity error occurred";

        // Detect specific known cases - adapt these checks to your DB's
        // messages/constraint names
        if (rootMessage.contains("unique") || rootMessage.contains("duplicate")
                || rootMessage.contains("unique constraint")
                || rootMessage.contains("duplicate key") || rootMessage.contains("constraint")) {

            // Example: Postgres unique constraint message often contains the constraint
            // name
            if (rootMessage.contains("applied_ipo_id") || rootMessage.contains("alloted_ipo_applied_ipo_id_key")) {
                code = "ALLOTMENT_ALREADY_EXISTS";
                userMessage = "This Applied IPO already has an allotment.";
            } else {
                // Generic duplicate/unique message
                code = "DUPLICATE_RESOURCE";
                userMessage = "Duplicate resource or unique constraint violated.";
            }
        } else if (rootMessage.contains("foreign key") || rootMessage.contains("violates foreign key")) {
            code = "FOREIGN_KEY_VIOLATION";
            userMessage = "Related resource not found or cannot be deleted (foreign key constraint).";
        } else {
            // fallback: include brief root message if safe
            String brief = rootMessage.length() > 200 ? rootMessage.substring(0, 200) + "..." : rootMessage;
            if (!brief.isBlank()) {
                userMessage = "Data integrity error: " + brief;
            } else {
                userMessage = "A data integrity error occurred.";
            }
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildErrorResponse(code, userMessage, HttpStatus.CONFLICT));
    }

    @ExceptionHandler(InvalidEmailAddressException.class)
    public ResponseEntity<?> handleInvalidEmail(InvalidEmailAddressException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(MailDeliveryException.class)
    public ResponseEntity<?> handleMailDelivery(MailDeliveryException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
    }

    @ExceptionHandler(EmailFormattingException.class)
    public ResponseEntity<?> handleFormatting(EmailFormattingException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleOtherExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(
                        "INTERNAL_SERVER_ERROR",
                        "Something went wrong on the server",
                        HttpStatus.INTERNAL_SERVER_ERROR));
    }

}