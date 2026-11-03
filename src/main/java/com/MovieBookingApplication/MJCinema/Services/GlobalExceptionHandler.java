package com.MovieBookingApplication.MJCinema.Services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice //for error checking.
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex){
        //the argument is the message displayed in the code when you throw runtime exception.

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now()); //request time
        body.put("message", ex.getMessage()); //argument
        body.put("status", HttpStatus.BAD_REQUEST.value());//error code

        return new  ResponseEntity<> (body, HttpStatus.BAD_REQUEST);
    }
    //a logged-in user who is not allowed (e.g. a normal user calling an admin endpoint) gets 403, not 400.
    //this must be its own handler: AccessDeniedException is a RuntimeException, so the handler above would catch it.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex){
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "You do not have permission to do this.");
        body.put("status", HttpStatus.FORBIDDEN.value());

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        errors.put("timestamp", LocalDateTime.now());
        errors.put("status", HttpStatus.BAD_REQUEST.value());


        // writes the DTO messages
        String errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        errors.put("message", errorMessage);

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

}
