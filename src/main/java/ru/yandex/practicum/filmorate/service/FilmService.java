package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {
    List<Film> getAllFilms();

    Film getFilm(int id);

    Film addFilm(Film film);

    Film updateFilm(Film film);

    void like(int id, int userId);

    void deleteLike(int id, int userId);

    List<Film> getTopLikes(int count);

    List<Film> getDirectorFilms(int directorId, String sortBy);
}