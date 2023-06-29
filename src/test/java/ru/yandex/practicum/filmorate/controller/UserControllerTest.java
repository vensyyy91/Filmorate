package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.Repository;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private Repository<User> repository;
    @Autowired
    private UserService service;
    private static User user1;
    private static User user2;
    private static ObjectMapper mapper;
    private static final String USERS_PATH = "/users";

    @BeforeAll
    public static void beforeAll() {
        user1 = new User(1,"user1@yandex.ru", "user1", "Petr",
                LocalDate.of(1990, 12, 7));
        user2 = new User(2,"user2@yandex.ru", "user2", "",
                LocalDate.of(1989, 8, 14));
        mapper = new ObjectMapper()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .findAndRegisterModules();
    }

    @BeforeEach
    public void beforeEach() {
        service.setId(0);
    }

    @AfterEach
    public void afterEach() {
        repository.getMap().clear();
    }

    @Test
    public void getAllUsers() throws Exception {
        service.addUser(user1);
        service.addUser(user2);

        mvc.perform(get(USERS_PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(List.of(user1, user2))));
    }

    @Test
    public void addUserWithValidRequest() throws Exception {
        mvc.perform(post(USERS_PATH).content(mapper.writeValueAsString(user1)).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(user1)));

        assertEquals(user1, service.getAllUsers().get(0));
    }

    @Test
    public void addUserWithInvalidEmail() throws Exception {
        User user3 = new User("user3yandex.ru", "user3", "Igor",
                LocalDate.of(1990, 3, 10));

        mvc.perform(post(USERS_PATH).content(mapper.writeValueAsString(user3)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        assertFalse(service.getAllUsers().contains(user3));
    }

    @Test
    public void addUserWithInvalidLogin() throws Exception {
        User user3 = new User("user3@yandex.ru", "user 3", "Igor",
                LocalDate.of(1990, 3, 10));

        mvc.perform(post(USERS_PATH).content(mapper.writeValueAsString(user3)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        assertFalse(service.getAllUsers().contains(user3));
    }

    @Test
    public void addUserWithInvalidBirthday() throws Exception {
        User user3 = new User("user3@yandex.ru", "user3", "Igor",
                LocalDate.of(2025, 3, 10));

        mvc.perform(post(USERS_PATH).content(mapper.writeValueAsString(user3)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

        assertFalse(service.getAllUsers().contains(user3));
    }

    @Test
    public void addUserWithEmptyName() throws Exception {
        User user3 = new User("user3@yandex.ru", "user3", "",
                LocalDate.of(1990, 3, 10));

        mvc.perform(post(USERS_PATH).content(mapper.writeValueAsString(user3)).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.name", is("user3")));

        assertEquals("user3", service.getAllUsers().get(0).getName());
    }

    @Test
    public void addUserWithEmptyBody() throws Exception {
        mvc.perform(post(USERS_PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void updateUserWithExistingId() throws Exception {
        service.addUser(user1);
        User updatedUser = new User(1,"user1@google.com", "user1", "Petya",
                LocalDate.of(1990,12,8));

        mvc.perform(put(USERS_PATH).content(mapper.writeValueAsString(updatedUser)).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(updatedUser)));

        assertEquals("user1@google.com", service.getAllUsers().get(0).getEmail());
        assertEquals("Petya", service.getAllUsers().get(0).getName());
        assertEquals(LocalDate.of(1990,12,8), service.getAllUsers().get(0).getBirthday());
    }

    @Test
    public void updateUserWithNonExistingId() throws Exception {
        service.addUser(user1);
        User updatedUser = new User(777,"user1@google.com", "user1", "Petya",
                LocalDate.of(1990,12,8));

        mvc.perform(put(USERS_PATH).content(mapper.writeValueAsString(updatedUser)).contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound());

        assertEquals("user1@yandex.ru", service.getAllUsers().get(0).getEmail());
        assertEquals("Petr", service.getAllUsers().get(0).getName());
        assertEquals(LocalDate.of(1990,12,7), service.getAllUsers().get(0).getBirthday());
    }

    @Test
    public void updateUserWithEmptyBody() throws Exception {
        mvc.perform(put(USERS_PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}