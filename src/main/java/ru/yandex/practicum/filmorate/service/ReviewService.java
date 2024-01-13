package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewService {
    Review addReview(Review review);

    Review updateReview(Review review);

    void deleteReview(int id);

    Review getReviewById(int id);

    List<Review> getAllReviews(Integer filmId, int count);

    void addLike(int id, int userId);

    void addDislike(int id, int userId);

    void deleteLike(int id, int userId);

    void deleteDislike(int id, int userId);
}