package com.example.coursemanagementsystem.exception;

import com.example.coursemanagementsystem.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalException {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto>handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){

        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponseDto error=ErrorResponseDto.builder()
                .success(false)
                .message(ex.getMessage())
                .error(ex.getClass().getSimpleName())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFoundException(ResourceNotFoundException ex){
        ErrorResponseDto error=ErrorResponseDto.builder()
                .success(false)
                .message(ex.getMessage())
                .error(ex.getClass().getSimpleName())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InactiveCourseException.class)
    public ResponseEntity<ErrorResponseDto> handleInactiveCourseException(InactiveCourseException ex){
        ErrorResponseDto error=ErrorResponseDto.builder()
                .success(false)
                .message(ex.getMessage())
                .error(ex.getClass().getSimpleName())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AlreadyEnrolledException.class)
    public ResponseEntity<ErrorResponseDto> handleAlreadyEnrolledException(AlreadyEnrolledException ex){
        ErrorResponseDto error=ErrorResponseDto.builder()
                .success(false)
                .message(ex.getMessage())
                .error(ex.getClass().getSimpleName())
                .statusCode(HttpStatus.CONFLICT.value())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex){
        ErrorResponseDto error=ErrorResponseDto.builder()
                .success(false)
                .message(ex.getMessage())
                .error(ex.getClass().getSimpleName())
                .statusCode(HttpStatus.CONFLICT.value())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(AccessDeniedException ex){
        ErrorResponseDto error=ErrorResponseDto.builder()
                .success(false)
                .message(ex.getMessage())
                .error(ex.getClass().getSimpleName())
                .statusCode(HttpStatus.FORBIDDEN.value())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntime(RuntimeException ex){
        ErrorResponseDto error=ErrorResponseDto.builder()
                .success(false)
                .message(ex.getMessage())
                .error(ex.getClass().getSimpleName())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex){
        ErrorResponseDto error=ErrorResponseDto.builder()
                .success(false)
                .message(ex.getMessage())
                .error(ex.getClass().getSimpleName())
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}
