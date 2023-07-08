package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.Repository;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private Repository<Film> repository;
    @Autowired
    private FilmService service;
    private static Film film1;
    private static Film film2;
    private static ObjectMapper mapper;
    private static final String FILMS_PATH = "/films";

    @BeforeAll
    public static void beforeAll() {
        film1 = new Film(1,"film1", "Blockbuster",
                LocalDate.of(2005, 7, 13), 120);
        film2 = new Film(2,"film2", "Drama",
                LocalDate.of(2010, 10, 21), 150);
        mapper = new ObjectMapper()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .findAndRegisterModules();
    }

    @Test
    public void getAllFilms() throws Exception {
        service.addFilm(film1);
        service.addFilm(film2);

        mvc.perform(get(FILMS_PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(List.of(film1, film2))));
    }

    @Test
    public void addFilmWithValidRequest() throws Exception {
        mvc.perform(post(FILMS_PATH).content(mapper.writeValueAsString(film1)).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(film1)));

        assertEquals(film1, service.getAllFilms().get(0));
    }

    @Test
    public void addFilmWithInvalidName() throws Exception {
        Film film3 = new Film("", "Best Film Ever",
                LocalDate.of(1998, 3, 15), 180);

        mvc.perform(post(FILMS_PATH).content(mapper.writeValueAsString(film3)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        assertFalse(service.getAllFilms().contains(film3));
    }

    @Test
    public void addFilmWithInvalidDescription() throws Exception {
        Film film3 = new Film("Harry Potter and the Sorcecer's Stone",
                "Adaptation of the first of J.K. Rowling's popular children's novels about Harry Potter, " +
                        "a boy who learns on his eleventh birthday that he is the orphaned son of two powerful wizards" +
                        " and possesses unique magical powers of his own.",
                LocalDate.of(2001, 11, 16), 152);

        mvc.perform(post(FILMS_PATH).content(mapper.writeValueAsString(film3)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        assertFalse(service.getAllFilms().contains(film3));
    }

    @Test
    public void addFilmWithInvalidReleaseDate() throws Exception {
        Film film3 = new Film("film3", "Best Film Ever",
                LocalDate.of(1882, 3, 15), 180);

        mvc.perform(post(FILMS_PATH).content(mapper.writeValueAsString(film3)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        assertFalse(service.getAllFilms().contains(film3));
    }

    @Test
    public void addFilmWithInvalidDuration() throws Exception {
        Film film3 = new Film("film3", "Best Film Ever",
                LocalDate.of(1998, 3, 15), 0);

        mvc.perform(post(FILMS_PATH).content(mapper.writeValueAsString(film3)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        assertFalse(service.getAllFilms().contains(film3));
    }

    @Test
    public void addFilmWithEmptyBody() throws Exception {
        mvc.perform(post(FILMS_PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void updateFilmWithExistingId() throws Exception {
        service.addFilm(film1);
        Film updatedFilm = new Film(1,"film1", "Super blockbuster",
                LocalDate.of(2004, 7, 13), 130);

        mvc.perform(put(FILMS_PATH).content(mapper.writeValueAsString(updatedFilm)).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(updatedFilm)));

        assertEquals("Super blockbuster", service.getAllFilms().get(0).getDescription());
        assertEquals(LocalDate.of(2004, 7, 13), service.getAllFilms().get(0).getReleaseDate());
        assertEquals(130, service.getAllFilms().get(0).getDuration());
    }

    @Test
    public void updateFilmWithNonExistingId() throws Exception {
        service.addFilm(film1);
        Film updatedFilm = new Film(777,"film1", "Super blockbuster",
                LocalDate.of(2004, 7, 13), 130);

        mvc.perform(put(FILMS_PATH).content(mapper.writeValueAsString(updatedFilm)).contentType(MediaType.APPLICATION_JSON))
                            .andExpect(status().isNotFound());

        assertEquals("Blockbuster", service.getAllFilms().get(0).getDescription());
        assertEquals(LocalDate.of(2005, 7, 13), service.getAllFilms().get(0).getReleaseDate());
        assertEquals(120, service.getAllFilms().get(0).getDuration());
    }

    @Test
    public void updateFilmWithEmptyBody() throws Exception {
        mvc.perform(put(FILMS_PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}