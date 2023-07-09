package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryFilmRepository implements Repository<Film> {
    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public Map<Integer, Film> getMap() {
        return films;
    }

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film get(int id) {
        if (films.get(id) == null) {
            throw new FilmNotFoundException(String.format("Фильм с id=%d не найден.", id));
        }
        return films.get(id);
    }

    @Override
    public void save(int id, Film film) {
        films.put(id, film);
    }
}