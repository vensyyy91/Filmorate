package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public Review add(@RequestBody @Valid Review review) {
        log.info("Получен запрос POST /reviews");
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review update(@RequestBody @Valid Review review) {
        log.info("Получен запрос PUT /reviews");
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        log.info("Получен запрос DELETE /reviews/{}", id);
        reviewService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public Review get(@PathVariable int id) {
        log.info("Получен запрос GET /reviews/{}", id);
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public List<Review> getAll(@RequestParam(required = false) Integer filmId,
                                   @RequestParam(defaultValue = "10") int count) {
        log.info("Получен запрос GET /reviews");
        return reviewService.getAllReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Получен запрос PUT /reviews/{}/like/{}", id, userId);
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable int id, @PathVariable int userId) {
        log.info("Получен запрос PUT /reviews/{}/dislike/{}", id, userId);
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Получен запрос DELETE /reviews/{}/like/{}", id, userId);
        reviewService.deleteLikeOrDislike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable int id, @PathVariable int userId) {
        log.info("Получен запрос DELETE /reviews/{}/dislike/{}", id, userId);
        reviewService.deleteLikeOrDislike(id, userId);
    }
}