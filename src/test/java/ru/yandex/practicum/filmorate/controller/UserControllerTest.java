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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.Repository;
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
class UserControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private Repository<User> repository;
    @Autowired
    private UserService service;
    private User user1;
    private User user2;
    private static ObjectMapper mapper;
    private static final String USERS_PATH = "/users";

    @BeforeAll
    public static void beforeAll() {
        mapper = new ObjectMapper()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .findAndRegisterModules();
    }

    @BeforeEach
    public void beforeEach() {
        user1 = new User(1,"user1@yandex.ru", "user1", "Petr",
                LocalDate.of(1990, 12, 7), new HashSet<>());
        user2 = new User(2,"user2@yandex.ru", "user2", "",
                LocalDate.of(1989, 8, 14), new HashSet<>());
        service.addUser(user1);
        service.addUser(user2);
    }

    @Test
    public void getAllUsers() throws Exception {
        mvc.perform(get(USERS_PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(List.of(user1, user2))));
    }

    @Test
    public void getAllUsersWhenEmpty() throws Exception {
        repository.getMap().clear();

        mvc.perform(get(USERS_PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(Collections.emptyList())));
    }

    @Test
    public void getUser() throws Exception {
         mvc.perform(get(USERS_PATH + "/1").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(user1)));
    }

    @Test
    public void getUserWithInvalidId() throws Exception {
        mvc.perform(get(USERS_PATH + "/999").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("Пользователь с id=999 не найден.")));
    }

    @Test
    public void addUserWithValidRequest() throws Exception {
        User user3 = new User(3, "user3@yandex.ru", "user3", "Igor",
                LocalDate.of(1990, 3, 10), new HashSet<>());

        mvc.perform(post(USERS_PATH).content(mapper.writeValueAsString(user3)).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(user3)));

        assertEquals(user3, service.getAllUsers().get(2));
    }

    @Test
    public void addUserWithInvalidEmail() throws Exception {
        User user3 = new User("user3yandex.ru", "user3", "Igor",
                LocalDate.of(1990, 3, 10));

        mvc.perform(post(USERS_PATH).content(mapper.writeValueAsString(user3)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertFalse(service.getAllUsers().contains(user3));
    }

    @Test
    public void addUserWithInvalidLogin() throws Exception {
        User user3 = new User("user3@yandex.ru", "user 3", "Igor",
                LocalDate.of(1990, 3, 10));

        mvc.perform(post(USERS_PATH).content(mapper.writeValueAsString(user3)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertFalse(service.getAllUsers().contains(user3));
    }

    @Test
    public void addUserWithInvalidBirthday() throws Exception {
        User user3 = new User("user3@yandex.ru", "user3", "Igor",
                LocalDate.of(2025, 3, 10));

        mvc.perform(post(USERS_PATH).content(mapper.writeValueAsString(user3)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

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

        assertEquals("user3", service.getAllUsers().get(2).getName());
    }

    @Test
    public void addUserWithEmptyBody() throws Exception {
        mvc.perform(post(USERS_PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateUserWithExistingId() throws Exception {
        User updatedUser = new User(1,"user1@google.com", "user1", "Petya",
                LocalDate.of(1990,12,8), new HashSet<>());

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
        User updatedUser = new User(999,"user1@google.com", "user1", "Petya",
                LocalDate.of(1990,12,8), new HashSet<>());

        mvc.perform(put(USERS_PATH).content(mapper.writeValueAsString(updatedUser)).contentType(MediaType.APPLICATION_JSON))
                                .andExpectAll(status().isNotFound(),
                                        content().contentType(MediaType.APPLICATION_JSON),
                                        jsonPath("$.message", is("Пользователя с id=999 не существует.")));

        assertEquals("user1@yandex.ru", service.getAllUsers().get(0).getEmail());
        assertEquals("Petr", service.getAllUsers().get(0).getName());
        assertEquals(LocalDate.of(1990,12,7), service.getAllUsers().get(0).getBirthday());
    }

    @Test
    public void updateUserWithEmptyBody() throws Exception {
        mvc.perform(put(USERS_PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addFriend() throws Exception {
        mvc.perform(put(USERS_PATH + "/1/friends/2").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertTrue(user1.getFriends().contains(2));
        assertTrue(user2.getFriends().contains(1));
    }

    @Test
    public void addFriendWithInvalidId() throws Exception {
        mvc.perform(put(USERS_PATH + "/1/friends/999").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("Пользователь с id=999 не найден.")));

        assertFalse(user1.getFriends().contains(999));
    }

    @Test
    public void deleteFriend() throws Exception {
        service.addFriend(1, 2);

        mvc.perform(delete(USERS_PATH + "/1/friends/2").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertFalse(user1.getFriends().contains(2));
        assertFalse(user2.getFriends().contains(1));
    }

    @Test
    public void deleteFriendWithInvalidId() throws Exception {
        service.addFriend(1, 2);

        mvc.perform(delete(USERS_PATH + "/1/friends/999").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("Пользователь с id=999 не найден.")));

        assertTrue(user1.getFriends().contains(2));
        assertTrue(user2.getFriends().contains(1));
    }

    @Test
    public void getAllFriends() throws Exception {
        User user3 = new User(3, "user3@yandex.ru", "user3", "Igor",
                LocalDate.of(1990, 3, 10), new HashSet<>());
        service.addUser(user3);
        service.addFriend(1,2);
        service.addFriend(1,3);

        mvc.perform(get(USERS_PATH + "/1/friends").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(List.of(user2, user3))));
    }

    @Test
    public void getAllFriendsWhenEmpty() throws Exception {
        mvc.perform(get(USERS_PATH + "/1/friends").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(Collections.emptyList())));
    }

    @Test
    public void getAllFriendsWithInvalidId() throws Exception {
        mvc.perform(get(USERS_PATH + "/999/friends").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("Пользователь с id=999 не найден.")));
    }

    @Test
    public void getCommonFriends() throws Exception {
        User user3 = new User(3, "user3@yandex.ru", "user3", "Igor",
                LocalDate.of(1990, 3, 10), new HashSet<>());
        service.addUser(user3);
        service.addFriend(1,2);
        service.addFriend(1,3);
        service.addFriend(2,3);

        mvc.perform(get(USERS_PATH + "/1/friends/common/2").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(Collections.singletonList(user3))));
    }

    @Test
    public void getCommonFriendsWithNoCommonFriends() throws Exception {
        service.addFriend(1,2);

        mvc.perform(get(USERS_PATH + "/1/friends/common/2").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(Collections.emptyList())));
    }

    @Test
    public void getCommonFriendsWhenEmpty() throws Exception {
        mvc.perform(get(USERS_PATH + "/1/friends/common/2").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().string(mapper.writeValueAsString(Collections.emptyList())));
    }

    @Test
    public void getCommonFriendsWithInvalidId() throws Exception {
        mvc.perform(get(USERS_PATH + "/1/friends/common/999").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        jsonPath("$.message", is("Пользователь с id=999 не найден.")));
    }
}