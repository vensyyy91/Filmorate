package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.Repository;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private Repository<Film> filmRepository;
    @Autowired
    private Repository<User> userRepository;
    @Autowired
    private FilmService filmService;
    @Autowired
    private UserService userService;
    private Film film1;
    private Film film2;
    private static ObjectMapper mapper;
    private static final String FILMS_PATH = "/films";

    @BeforeAll
    public static void beforeAll() {
        mapper = new ObjectMapper()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .findAndRegisterModules();
    }

    @BeforeEach
    public void beforeEach() {
        film1 = new Film(1,"film1", "Blockbuster",
                LocalDate.of(2005, 7, 13), 120, new HashSet<>());
        film2 = new Film(2,"film2", "Drama",
                LocalDate.of(2010, 10, 21), 150, new HashSet<>());
        filmService.addFilm(film1);
        filmService.addFilm(film2);
        User user1 = new User(1, "user1@yandex.ru", "user1", "Petr",
                LocalDate.of(1990, 12, 7), new HashSet<>());
        User user2 = new User(2, "user2@yandex.ru", "user2", "",
                LocalDate.of(1989, 8, 14), new HashSet<>());
        userService.addUser(user1);
        userService.addUser(user2);
    }

    @Test
    public void getAllFilms() throws Exception {
        mvc.perform(get(FILMS_PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(List.of(film1, film2))));
    }

    @Test
    public void getAllFilmsWhenEmpty() throws Exception {
        filmRepository.getMap().clear();
        mvc.perform(get(FILMS_PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(Collections.emptyList())));
    }

    @Test
    public void getFilm() throws Exception {
        mvc.perform(get(FILMS_PATH + "/1").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(film1)));
    }

    @Test
    public void getFilmWithInvalidId() throws Exception {
        mvc.perform(get(FILMS_PATH + "/999").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("Фильм с id=999 не найден.")));
    }

    @Test
    public void addFilmWithValidRequest() throws Exception {
        Film film3 = new Film(3,"film3", "Best Film Ever",
                LocalDate.of(1998, 3, 15), 180, new HashSet<>());

        mvc.perform(post(FILMS_PATH).content(mapper.writeValueAsString(film3)).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(film3)));

        assertEquals(film3, filmService.getAllFilms().get(2));
    }

    @Test
    public void addFilmWithInvalidName() throws Exception {
        Film film3 = new Film("", "Best Film Ever",
                LocalDate.of(1998, 3, 15), 180);

        mvc.perform(post(FILMS_PATH).content(mapper.writeValueAsString(film3)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertFalse(filmService.getAllFilms().contains(film3));
    }

    @Test
    public void addFilmWithInvalidDescription() throws Exception {
        Film film3 = new Film("Harry Potter and the Sorcecer's Stone",
                "Adaptation of the first of J.K. Rowling's popular children's novels about Harry Potter, " +
                        "a boy who learns on his eleventh birthday that he is the orphaned son of two powerful wizards" +
                        " and possesses unique magical powers of his own.",
                LocalDate.of(2001, 11, 16), 152);

        mvc.perform(post(FILMS_PATH).content(mapper.writeValueAsString(film3)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertFalse(filmService.getAllFilms().contains(film3));
    }

    @Test
    public void addFilmWithInvalidReleaseDate() throws Exception {
        Film film3 = new Film("film3", "Best Film Ever",
                LocalDate.of(1882, 3, 15), 180);

        mvc.perform(post(FILMS_PATH).content(mapper.writeValueAsString(film3)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertFalse(filmService.getAllFilms().contains(film3));
    }

    @Test
    public void addFilmWithInvalidDuration() throws Exception {
        Film film3 = new Film("film3", "Best Film Ever",
                LocalDate.of(1998, 3, 15), 0);

        mvc.perform(post(FILMS_PATH).content(mapper.writeValueAsString(film3)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertFalse(filmService.getAllFilms().contains(film3));
    }

    @Test
    public void addFilmWithEmptyBody() throws Exception {
        mvc.perform(post(FILMS_PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateFilmWithExistingId() throws Exception {
        Film updatedFilm = new Film(1,"film1", "Super blockbuster",
                LocalDate.of(2004, 7, 13), 130, new HashSet<>());

        mvc.perform(put(FILMS_PATH).content(mapper.writeValueAsString(updatedFilm)).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(updatedFilm)));

        assertEquals("Super blockbuster", filmService.getAllFilms().get(0).getDescription());
        assertEquals(LocalDate.of(2004, 7, 13), filmService.getAllFilms().get(0).getReleaseDate());
        assertEquals(130, filmService.getAllFilms().get(0).getDuration());
    }

    @Test
    public void updateFilmWithNonExistingId() throws Exception {
        Film updatedFilm = new Film(999,"film1", "Super blockbuster",
                LocalDate.of(2004, 7, 13), 130, new HashSet<>());

        mvc.perform(put(FILMS_PATH).content(mapper.writeValueAsString(updatedFilm)).contentType(MediaType.APPLICATION_JSON))
                            .andExpectAll(status().isNotFound(),
                                    content().contentType(MediaType.APPLICATION_JSON),
                                    jsonPath("$.message", is("Фильма с id=999 не существует.")));

        assertEquals("Blockbuster", filmService.getAllFilms().get(0).getDescription());
        assertEquals(LocalDate.of(2005, 7, 13), filmService.getAllFilms().get(0).getReleaseDate());
        assertEquals(120, filmService.getAllFilms().get(0).getDuration());
    }

    @Test
    public void updateFilmWithEmptyBody() throws Exception {
        mvc.perform(put(FILMS_PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void like() throws Exception {
        mvc.perform(put(FILMS_PATH + "/1/like/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertTrue(film1.getLikes().contains(1));
    }

    @Test
    public void likeWithInvalidUserId() throws Exception {
        mvc.perform(put(FILMS_PATH + "/1/like/999").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("Пользователь с id=999 не найден.")));

        assertFalse(film1.getLikes().contains(999));
    }

    @Test
    public void likeWithInvalidFilmId() throws Exception {
        mvc.perform(put(FILMS_PATH + "/999/like/1").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("Фильм с id=999 не найден.")));
    }

    @Test
    public void deleteLike() throws Exception {
        filmService.like(1,1);

        mvc.perform(delete(FILMS_PATH + "/1/like/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertFalse(film1.getLikes().contains(1));
    }

    @Test
    public void deleteLikeWithNonExistingUserId() throws Exception {
        filmService.like(1,1);

        mvc.perform(delete(FILMS_PATH + "/1/like/999").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("Пользователь с id=999 не найден.")));

        assertTrue(film1.getLikes().contains(1));
    }

    @Test
    public void deleteLikeWithInvalidUserId() throws Exception {
        filmService.like(1,1);

        mvc.perform(delete(FILMS_PATH + "/1/like/2").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("Пользователь с id=2 не ставил лайк фильму с id=1.")));

        assertTrue(film1.getLikes().contains(1));
    }

    @Test
    public void deleteLikeWithInvalidFilmId() throws Exception {
        filmService.like(1,1);

        mvc.perform(delete(FILMS_PATH + "/999/like/1").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("Фильм с id=999 не найден.")));

        assertTrue(film1.getLikes().contains(1));
    }

    @Test
    public void getTopLikes() throws Exception {
        filmService.like(2,1);
        filmService.like(2,2);
        filmService.like(1,1);

        mvc.perform(get(FILMS_PATH + "/popular").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(List.of(film2, film1))));
    }

    @Test
    public void getTopLikesWithCountParamLessThenFilmsNumber() throws Exception {
        filmService.like(2,1);
        filmService.like(2,2);
        filmService.like(1,1);

        mvc.perform(get(FILMS_PATH + "/popular?count=1").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(Collections.singletonList(film2))));
    }

    @Test
    public void getTopLikesWhenEmpty() throws Exception {
        mvc.perform(get(FILMS_PATH + "/popular").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(List.of(film1, film2))));
    }

    @Test
    public void getTopLikesWhenEqual() throws Exception {
        filmService.like(2,1);
        filmService.like(1,1);

        mvc.perform(get(FILMS_PATH + "/popular").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(List.of(film1, film2))));
    }
}