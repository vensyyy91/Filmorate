package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewDao {
    List<Review> getAll(Integer filmId, int count);

    Review getById(int id);
    
    Review save(Review review);

    Review update(Review review);

    void delete(int id);

    void addLike(int id, int userId);

    void addDislike(int id, int userId);

    void deleteLike(int id, int userId);

    void deleteDislike(int id, int userId);

    int getReviewUsefulRating(int id);
}