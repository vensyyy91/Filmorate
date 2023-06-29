package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.yandex.practicum.filmorate.model.Response;

import java.util.StringJoiner;

@RestControllerAdvice
@Slf4j
public class ExceptionControllerHandler {

    @ExceptionHandler
    public ResponseEntity<Response> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        StringJoiner joiner = new StringJoiner("; ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ex.getBindingResult().getFieldErrorCount(); i++) {
            FieldError error = ex.getBindingResult().getFieldErrors().get(i);
            String field = error.getField();
            builder.append("Неверно заполнено поле ").append(field).append(": ").append(error.getDefaultMessage());
            String fieldMessage = builder.toString();
            joiner.add(fieldMessage);
            builder.setLength(0);
        }
        String message = joiner.toString();
        Response response = new Response(message);
        log.error(message, ex);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<Response> handleValidationException(ValidationException ex) {
        Response response = new Response(ex.getMessage());
        log.error(ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
