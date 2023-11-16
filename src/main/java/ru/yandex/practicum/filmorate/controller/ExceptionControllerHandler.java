package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Response;

import java.util.StringJoiner;

@RestControllerAdvice
@Slf4j
public class ExceptionControllerHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        StringJoiner joiner = new StringJoiner("; ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ex.getBindingResult().getFieldErrorCount(); i++) {
            FieldError error = ex.getBindingResult().getFieldErrors().get(i);
            String field = error.getField();
            builder.append("Неверно заполнено поле ").append(field).append(": ").append(error.getDefaultMessage());
            joiner.add(builder.toString());
            builder.setLength(0);
        }
        String message = joiner.toString();
        log.error("Получен запрос с некорректными данными.");
        log.error(message);

        return new Response(message);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error(ex.getMessage());
        return new Response(ex.getMessage());
    }

    @ExceptionHandler({UserNotFoundException.class, FilmNotFoundException.class,
            GenreNotFoundException.class, MpaNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Response handleNotFoundException(RuntimeException ex) {
        log.error(ex.getMessage());
        return new Response(ex.getMessage());
    }

    /*@ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response handleAnyAnotherException(Throwable ex) {
        log.error("Произошла непредвиденная ошибка: " + ex.getMessage());
        return new Response("Произошла непредвиденная ошибка: " + ex.getMessage());
    }*/
}