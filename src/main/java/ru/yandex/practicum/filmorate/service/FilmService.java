package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {
    List<Film> getAllFilms();

    Film getFilm(int id);

    Film addFilm(Film film);

    Film updateFilm(Film film);

    void addMark(int id, int userId, int mark);

    void deleteMark(int id, int userId);

    List<Film> getTopRating(int count);
}