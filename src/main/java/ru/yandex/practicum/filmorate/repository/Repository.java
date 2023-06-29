package ru.yandex.practicum.filmorate.repository;

import java.util.List;
import java.util.Map;

public interface Repository<T> {
    Map<Integer, T> getMap();
    List<T> getAll();
    void save(int id, T t);
}
