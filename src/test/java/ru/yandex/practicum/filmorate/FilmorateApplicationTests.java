package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dao.*;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.model.event.Event;
import ru.yandex.practicum.filmorate.model.event.EventType;
import ru.yandex.practicum.filmorate.model.event.Operation;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmorateApplicationTests {
	private final JdbcTemplate jdbcTemplate;
	private final FilmDao filmDao;
	private final UserDao userDao;
	private final GenreDao genreDao;
	private final MpaDao mpaDao;
	private final LikesDao likesDao;
	private final FriendsDao friendsDao;
	private final DirectorDao directorDao;
	private final ReviewDao reviewDao;
	private final EventDao eventDao;
	private static final Genre GENRE_COMEDY = new Genre(1, "Комедия");
	private static final Genre GENRE_DRAMA = new Genre(2, "Драма");
	private static final Genre GENRE_CARTOON = new Genre(3, "Мультфильм");
	private static final Genre GENRE_THRILLER = new Genre(4, "Триллер");
	private static final Genre GENRE_DOCUMENTARY = new Genre(5, "Документальный");
	private static final Genre GENRE_ACTION = new Genre(6, "Боевик");
	private static final Mpa MPA_G = new Mpa(1, "G");
	private static final Mpa MPA_PG = new Mpa(2, "PG");
	private static final Mpa MPA_PG_13 = new Mpa(3, "PG-13");
	private static final Mpa MPA_R = new Mpa(4, "R");
	private static final Mpa MPA_NC_17 = new Mpa(5, "NC-17");


	@BeforeEach
	public void init() {
		String sqlAddFilms = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES " +
				"('film1', 'first test film', '1990-09-10', 150, 2), " +
				"('film2', 'second test film', '2005-12-04', 120, 1), " +
				"('film3', 'third test film', '2008-10-01', 180, 4)";
		String sqlAddUsers = "INSERT INTO users (email, login, name, birthday) VALUES " +
				"('1@yandex.ru', 'user1', 'first', '1992-03-04'), " +
				"('2@yandex.ru', 'user2', 'second', '1994-10-14'), " +
				"('3@yandex.ru', 'user3', 'third', '1996-06-20')";
		String sqlAddGenres = "INSERT INTO film_genre (film_id, genre_id) " +
				"VALUES (1, 1), (2, 4), (3, 2), (3, 4), (3, 6)";
		String sqlAddLikes = "INSERT INTO likes (film_id, user_id) " +
				"VALUES (1, 2), (2, 1), (2, 2), (3, 1), (3, 2), (3, 3)";
		String sqlAddFriends = "INSERT INTO friends (user_id, friend_id) " +
				"VALUES (1, 2), (1, 3), (2, 3), (3, 1)";
		String sqlAddDirectors = "INSERT INTO directors (director_name) VALUES ('director1'), ('director2')";
		String sqlAddFilmDirectors = "INSERT INTO film_director (film_id, director_id) VALUES (1, 1), (2, 1), (2, 2)";
		String sqlAddReviews = "INSERT INTO reviews (content, is_positive, user_id, film_id) VALUES " +
				"('very good film', true, 1, 2), " +
				"('the film is too bad', false, 2, 2), " +
				"('the film is nice', true, 3, 3)";
		String sqlAddReviewLikes = "INSERT INTO review_like (user_id, review_id, is_positive) " +
				"VALUES (2, 1, false), (1, 3, true), (2, 3, true)";
		jdbcTemplate.update(sqlAddFilms);
		jdbcTemplate.update(sqlAddUsers);
		jdbcTemplate.update(sqlAddGenres);
		jdbcTemplate.update(sqlAddLikes);
		jdbcTemplate.update(sqlAddFriends);
		jdbcTemplate.update(sqlAddDirectors);
		jdbcTemplate.update(sqlAddFilmDirectors);
		jdbcTemplate.update(sqlAddReviews);
		jdbcTemplate.update(sqlAddReviewLikes);
	}

	@Test
	public void getAllFilms() {
		List<Film> films = filmDao.getAll();

		assertThat(films).hasSize(3);
		assertThat(films.get(0)).isEqualTo(new Film(1, "film1", "first test film",
				LocalDate.of(1990, 9, 10), 150, 1,
				Set.of(GENRE_COMEDY), MPA_PG, Collections.singleton(new Director(1, "director1"))));
		assertThat(films.get(1)).isEqualTo(new Film(2, "film2", "second test film",
				LocalDate.of(2005, 12, 4), 120, 2,
				Set.of(GENRE_THRILLER), MPA_G, Set.of(new Director(1, "director1"), new Director(2, "director2"))));
		assertThat(films.get(2)).isEqualTo(new Film(3, "film3", "third test film",
				LocalDate.of(2008, 10, 1), 180, 3,
				Set.of(GENRE_DRAMA, GENRE_THRILLER, GENRE_ACTION), MPA_R, Collections.emptySet()));
	}

	@Test
	public void getAllFilmsWhenEmpty() {
		jdbcTemplate.update("DELETE FROM film_genre");
		jdbcTemplate.update("DELETE FROM film_director");
		jdbcTemplate.update("DELETE FROM likes");
		jdbcTemplate.update("DELETE FROM films");
		List<Film> films = filmDao.getAll();

		assertThat(films).hasSize(0);
	}

	@Test
	public void getFilmById() {
		Film film = filmDao.getById(1);

		assertThat(film).isNotNull();
		assertThat(film).hasFieldOrPropertyWithValue("id", 1);
	}

	@Test
	public void getFilmByNonExistentId() {
		assertThatThrownBy(() -> filmDao.getById(999)).hasMessage("Фильм с id=999 не найден.");
	}

	@Test
	public void addFilm() {
		Film film = new Film("film4", "fourth test film",
				LocalDate.of(2010, 8, 20), 140,
				Set.of(GENRE_COMEDY, GENRE_CARTOON), MPA_NC_17);
		Film filmReturned = filmDao.save(film);
		Film filmFromDb = filmDao.getById(4);

		assertThat(filmReturned).isNotNull();
		assertThat(filmReturned).hasFieldOrPropertyWithValue("id", 4);
		assertThat(filmReturned.getGenres()).hasSize(2);
		assertThat(filmReturned.getGenres()).contains(GENRE_COMEDY, GENRE_CARTOON);
		assertThat(filmFromDb).isNotNull();
		assertThat(filmFromDb).isEqualTo(filmReturned);
		assertThat(filmFromDb).hasFieldOrPropertyWithValue("id", 4);
		assertThat(filmFromDb).hasFieldOrPropertyWithValue("name", "film4");
		assertThat(filmFromDb.getGenres()).hasSize(2);
		assertThat(filmFromDb.getGenres()).contains(GENRE_COMEDY, GENRE_CARTOON);
	}

	@Test
	public void addFilmWithDuplicateGenre() {
		Film film = new Film("film4", "fourth test film",
				LocalDate.of(2010, 8, 20), 140,
				new TreeSet<>(List.of(GENRE_COMEDY, GENRE_CARTOON, GENRE_COMEDY)), MPA_NC_17);
		Film filmReturned = filmDao.save(film);
		Film filmFromDb = filmDao.getById(4);

		assertThat(filmReturned.getGenres()).hasSize(2);
		assertThat(filmReturned.getGenres()).contains(GENRE_COMEDY, GENRE_CARTOON);
		assertThat(filmFromDb.getGenres()).hasSize(2);
		assertThat(filmFromDb.getGenres()).contains(GENRE_COMEDY, GENRE_CARTOON);
	}

	@Test
	public void updateFilm() {
		Film updatedFilm = new Film(1, "updatedFilm1", "first test film updated",
				LocalDate.of(1990, 9, 10), 150,
				Set.of(GENRE_COMEDY, GENRE_CARTOON), MPA_PG_13);
		Film filmReturned = filmDao.save(updatedFilm);
		Film filmFromDb = filmDao.getById(1);

		assertThat(filmReturned).isNotNull();
		assertThat(filmReturned).hasFieldOrPropertyWithValue("id", 1);
		assertThat(filmReturned).hasFieldOrPropertyWithValue("name", "updatedFilm1");
		assertThat(filmReturned).hasFieldOrPropertyWithValue("mpa", MPA_PG_13);
		assertThat(filmReturned.getGenres()).hasSize(2);
		assertThat(filmReturned.getGenres()).contains(GENRE_COMEDY, GENRE_CARTOON);
		assertThat(filmFromDb).isNotNull();
		assertThat(filmFromDb).isEqualTo(filmReturned);
		assertThat(filmFromDb).hasFieldOrPropertyWithValue("id", 1);
		assertThat(filmFromDb).hasFieldOrPropertyWithValue("name", "updatedFilm1");
		assertThat(filmFromDb).hasFieldOrPropertyWithValue("mpa", MPA_PG_13);
		assertThat(filmFromDb.getGenres()).hasSize(2);
		assertThat(filmFromDb.getGenres()).contains(GENRE_COMEDY, GENRE_CARTOON);
	}

	@Test
	public void updateFilmWithFewerGenres() {
		Film updatedFilm = new Film(3, "updatedFilm3", "third test film",
				LocalDate.of(2008, 10, 1), 180,
				Set.of(GENRE_DRAMA, GENRE_THRILLER), MPA_R);
		Film filmReturned = filmDao.save(updatedFilm);
		Film filmFromDb = filmDao.getById(3);

		assertThat(filmReturned.getGenres()).hasSize(2);
		assertThat(filmReturned.getGenres()).contains(GENRE_DRAMA, GENRE_THRILLER);
		assertThat(filmFromDb.getGenres()).hasSize(2);
		assertThat(filmFromDb.getGenres()).contains(GENRE_DRAMA, GENRE_THRILLER);
	}

	@Test
	public void updateFilmWithDuplicateGenre() {
		Film updatedFilm = new Film(1, "updatedFilm1", "first test film updated",
				LocalDate.of(1990, 9, 10), 150,
				new TreeSet<>(List.of(GENRE_COMEDY, GENRE_CARTOON, GENRE_CARTOON, GENRE_COMEDY)), MPA_PG_13);
		Film filmReturned = filmDao.save(updatedFilm);
		Film filmFromDb = filmDao.getById(1);

		assertThat(filmReturned.getGenres()).hasSize(2);
		assertThat(filmReturned.getGenres()).contains(GENRE_COMEDY, GENRE_CARTOON);
		assertThat(filmFromDb.getGenres()).hasSize(2);
		assertThat(filmFromDb.getGenres()).contains(GENRE_COMEDY, GENRE_CARTOON);
	}

	@Test
	public void deleteFilm() {
		filmDao.delete(1);
		List<Film> films = filmDao.getAll();
		List<Film> top = filmDao.getTop(5, null, null);

		assertThatThrownBy(() -> filmDao.getById(1)).hasMessage("Фильм с id=1 не найден.");
		assertThat(films).hasSize(2);
		assertThat(top).hasSize(2);
	}

	@Test
	public void getAllUsers() {
		List<User> users = userDao.getAll();

		assertThat(users).hasSize(3);
		assertThat(users.get(0)).isEqualTo(new User(1, "1@yandex.ru", "user1", "first",
				LocalDate.of(1992, 3, 4)));
		assertThat(users.get(1)).isEqualTo(new User(2, "2@yandex.ru", "user2", "second",
				LocalDate.of(1994, 10, 14)));
		assertThat(users.get(2)).isEqualTo(new User(3, "3@yandex.ru", "user3", "third",
				LocalDate.of(1996, 6, 20)));
	}

	@Test
	public void getAllUsersWhenEmpty() {
		jdbcTemplate.update("DELETE FROM friends");
		jdbcTemplate.update("DELETE FROM likes");
		jdbcTemplate.update("DELETE FROM users");
		List<User> users = userDao.getAll();

		assertThat(users).hasSize(0);
	}

	@Test
	public void getUserById() {
		User user = userDao.getById(1);

		assertThat(user).isNotNull();
		assertThat(user).hasFieldOrPropertyWithValue("id", 1);
	}

	@Test
	public void getUserByNonExistentId() {
		assertThatThrownBy(() -> userDao.getById(999)).hasMessage("Пользователь с id=999 не найден.");
	}

	@Test
	public void addUser() {
		User user = new User("4@yandex.ru", "user4", "fourth",
				LocalDate.of(1998, 7, 16));
		User userReturned = userDao.save(user);
		User userFromDb = userDao.getById(4);

		assertThat(userReturned).isNotNull();
		assertThat(userReturned).hasFieldOrPropertyWithValue("id", 4);
		assertThat(userFromDb).isNotNull();
		assertThat(userFromDb).isEqualTo(userReturned);
		assertThat(userFromDb).hasFieldOrPropertyWithValue("id", 4);
		assertThat(userFromDb).hasFieldOrPropertyWithValue("email", "4@yandex.ru");
	}

	@Test
	public void updateUser() {
		User user = new User("4@yandex.ru", "user4", "fourth",
				LocalDate.of(1998, 7, 16));
		userDao.save(user);
		User updatedUser = new User(4, "4@google.com", "user4", "the fourth",
				LocalDate.of(1998, 7, 16));
		User userReturned = userDao.save(updatedUser);
		User userFromDb = userDao.getById(4);

		assertThat(userReturned).isNotNull();
		assertThat(userReturned).hasFieldOrPropertyWithValue("id", 4);
		assertThat(userReturned).hasFieldOrPropertyWithValue("email", "4@google.com");
		assertThat(userReturned).hasFieldOrPropertyWithValue("name", "the fourth");
		assertThat(userFromDb).isNotNull();
		assertThat(userFromDb).isEqualTo(userReturned);
		assertThat(userFromDb).hasFieldOrPropertyWithValue("id", 4);
		assertThat(userFromDb).hasFieldOrPropertyWithValue("email", "4@google.com");
		assertThat(userReturned).hasFieldOrPropertyWithValue("name", "the fourth");
	}

	@Test
	public void deleteUser() {
		userDao.delete(1);
		List<User> friends = friendsDao.getAllById(3);
		Film film2 = filmDao.getById(2);
		Film film3 = filmDao.getById(3);

		assertThatThrownBy(() -> userDao.getById(1)).hasMessage("Пользователь с id=1 не найден.");
		assertThat(friends).hasSize(0);
		assertThat(film2).hasFieldOrPropertyWithValue("rate", 1);
		assertThat(film3).hasFieldOrPropertyWithValue("rate", 2);
	}

	@Test
	public void getAllGenres() {
		List<Genre> genres = genreDao.getAll();

		assertThat(genres).hasSize(6);
		assertThat(genres.get(0)).isEqualTo(GENRE_COMEDY);
		assertThat(genres.get(1)).isEqualTo(GENRE_DRAMA);
		assertThat(genres.get(2)).isEqualTo(GENRE_CARTOON);
		assertThat(genres.get(3)).isEqualTo(GENRE_THRILLER);
		assertThat(genres.get(4)).isEqualTo(GENRE_DOCUMENTARY);
		assertThat(genres.get(5)).isEqualTo(GENRE_ACTION);
	}

	@Test
	public void getAllGenresWhenEmpty() {
		jdbcTemplate.update("DELETE FROM film_genre");
		jdbcTemplate.update("DELETE FROM genres");
		List<Genre> genres = genreDao.getAll();

		assertThat(genres).hasSize(0);
	}

	@Test
	public void getGenreById() {
		Genre genre = genreDao.getById(1);

		assertThat(genre).isNotNull();
		assertThat(genre).hasFieldOrPropertyWithValue("id", 1);
		assertThat(genre).hasFieldOrPropertyWithValue("name", "Комедия");
	}

	@Test
	public void getGenreByNonExistentId() {
		assertThatThrownBy(() -> genreDao.getById(999)).hasMessage("Жанр с id=999 не найден.");
	}

	@Test
	public void getAllGenresByFilmId() {
		Set<Genre> genres = genreDao.getAllByFilmId(3);

		assertThat(genres).hasSize(3);
		assertThat(genres).contains(GENRE_DRAMA, GENRE_THRILLER, GENRE_ACTION);
	}

	@Test
	public void getAllGenresByFilmIdWhenEmpty() {
		jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = 3");
		Set<Genre> genres = genreDao.getAllByFilmId(3);

		assertThat(genres).hasSize(0);
	}

	@Test
	public void getAllMpa() {
		List<Mpa> mpa = mpaDao.getAll();

		assertThat(mpa).hasSize(5);
		assertThat(mpa.get(0)).isEqualTo(MPA_G);
		assertThat(mpa.get(1)).isEqualTo(MPA_PG);
		assertThat(mpa.get(2)).isEqualTo(MPA_PG_13);
		assertThat(mpa.get(3)).isEqualTo(MPA_R);
		assertThat(mpa.get(4)).isEqualTo(MPA_NC_17);
	}

	@Test
	public void getAllMpaWhenEmpty() {
		jdbcTemplate.update("DELETE FROM film_genre");
		jdbcTemplate.update("DELETE FROM film_director");
		jdbcTemplate.update("DELETE FROM likes");
		jdbcTemplate.update("DELETE FROM films");
		jdbcTemplate.update("DELETE FROM mpa");
		List<Mpa> mpa = mpaDao.getAll();

		assertThat(mpa).hasSize(0);
	}

	@Test
	public void getMpaById() {
		Mpa mpa = mpaDao.getById(1);

		assertThat(mpa).isNotNull();
		assertThat(mpa).hasFieldOrPropertyWithValue("id", 1);
		assertThat(mpa).hasFieldOrPropertyWithValue("name", "G");
	}

	@Test
	public void getMpaByNonExistentId() {
		assertThatThrownBy(() -> mpaDao.getById(999)).hasMessage("Рейтинг с id=999 не найден.");
	}

	@Test
	public void getAllLikesByFilmId() {
		List<Integer> likes = likesDao.getAllByFilmId(2);

		assertThat(likes).hasSize(2);
		assertThat(likes.get(0)).isEqualTo(1);
		assertThat(likes.get(1)).isEqualTo(2);
	}

	@Test
	public void getAllLikesByFilmIdWhenEmpty() {
		jdbcTemplate.update("DELETE FROM likes WHERE film_id = 2");
		List<Integer> likes = likesDao.getAllByFilmId(2);

		assertThat(likes).hasSize(0);
	}

	@Test
	public void addLike() {
		likesDao.save(1, 3);
		Film film = filmDao.getById(1);
		List<Integer> likes = likesDao.getAllByFilmId(1);

		assertThat(film).hasFieldOrPropertyWithValue("rate", 2);
		assertThat(likes).contains(3);
	}

	@Test
	public void deleteLike() {
		likesDao.delete(1, 2);
		Film film = filmDao.getById(1);
		List<Integer> likes = likesDao.getAllByFilmId(1);

		assertThat(film).hasFieldOrPropertyWithValue("rate", 0);
		assertThat(likes).hasSize(0);
	}

	@Test
	public void getTopLikesAllFilms() {
		List<Film> topLikes = filmDao.getTop(10, null, null);

		assertThat(topLikes).hasSize(3);
		assertThat(topLikes.get(0)).isEqualTo(new Film(3, "film3", "third test film",
				LocalDate.of(2008, 10, 1), 180, 3,
				Set.of(GENRE_DRAMA, GENRE_THRILLER, GENRE_ACTION), MPA_R, Collections.emptySet()));
		assertThat(topLikes.get(1)).isEqualTo(new Film(2, "film2", "second test film",
				LocalDate.of(2005, 12, 4), 120, 2,
				Set.of(GENRE_THRILLER), MPA_G, Set.of(new Director(1, "director1"), new Director(2, "director2"))));
		assertThat(topLikes.get(2)).isEqualTo(new Film(1, "film1", "first test film",
				LocalDate.of(1990, 9, 10), 150, 1,
				Set.of(GENRE_COMEDY), MPA_PG, Collections.singleton(new Director(1, "director1"))));
	}

	@Test
	public void getTopLikesFewerThanFilms() {
		List<Film> topLikes = filmDao.getTop(2, null, null);

		assertThat(topLikes).hasSize(2);
		assertThat(topLikes.get(0)).isEqualTo(new Film(3, "film3", "third test film",
				LocalDate.of(2008, 10, 1), 180, 3,
				Set.of(GENRE_DRAMA, GENRE_THRILLER, GENRE_ACTION), MPA_R, Collections.emptySet()));
		assertThat(topLikes.get(1)).isEqualTo(new Film(2, "film2", "second test film",
				LocalDate.of(2005, 12, 4), 120, 2,
				Set.of(GENRE_THRILLER), MPA_G, Set.of(new Director(1, "director1"), new Director(2, "director2"))));
	}

	@Test
	public void getTopLikesWhenSomeWithNoLikes() {
		jdbcTemplate.update("DELETE FROM likes WHERE film_id IN (1, 2)");
		List<Film> topLikes = filmDao.getTop(10, null, null);

		assertThat(topLikes).hasSize(3);
		assertThat(topLikes.get(0)).isEqualTo(new Film(3, "film3", "third test film",
				LocalDate.of(2008, 10, 1), 180, 3,
				Set.of(GENRE_DRAMA, GENRE_THRILLER, GENRE_ACTION), MPA_R, Collections.emptySet()));
		assertThat(topLikes.get(1)).isEqualTo(new Film(1, "film1", "first test film",
				LocalDate.of(1990, 9, 10), 150, 0,
				Set.of(GENRE_COMEDY), MPA_PG, Collections.singleton(new Director(1, "director1"))));
		assertThat(topLikes.get(2)).isEqualTo(new Film(2, "film2", "second test film",
				LocalDate.of(2005, 12, 4), 120, 0,
				Set.of(GENRE_THRILLER), MPA_G, Set.of(new Director(1, "director1"), new Director(2, "director2"))));
	}

	@Test
	public void getTopLikesWhenAllWithNoLikes() {
		jdbcTemplate.update("DELETE FROM likes");
		List<Film> topLikes = filmDao.getTop(10, null, null);

		assertThat(topLikes).hasSize(3);
		assertThat(topLikes.get(0)).isEqualTo(new Film(1, "film1", "first test film",
				LocalDate.of(1990, 9, 10), 150, 0,
				Set.of(GENRE_COMEDY), MPA_PG, Collections.singleton(new Director(1, "director1"))));
		assertThat(topLikes.get(1)).isEqualTo(new Film(2, "film2", "second test film",
				LocalDate.of(2005, 12, 4), 120, 0,
				Set.of(GENRE_THRILLER), MPA_G, Set.of(new Director(1, "director1"), new Director(2, "director2"))));
		assertThat(topLikes.get(2)).isEqualTo(new Film(3, "film3", "third test film",
				LocalDate.of(2008, 10, 1), 180, 0,
				Set.of(GENRE_DRAMA, GENRE_THRILLER, GENRE_ACTION), MPA_R, Collections.emptySet()));
	}

	@Test
	public void getTopLikesWithGenreId() {
		List<Film> topLikes = filmDao.getTop(10, 4, null);

		assertThat(topLikes).hasSize(2);
		assertThat(topLikes.get(0)).isEqualTo(new Film(3, "film3", "third test film",
				LocalDate.of(2008, 10, 1), 180, 3,
				Set.of(GENRE_DRAMA, GENRE_THRILLER, GENRE_ACTION), MPA_R, Collections.emptySet()));
		assertThat(topLikes.get(1)).isEqualTo(new Film(2, "film2", "second test film",
				LocalDate.of(2005, 12, 4), 120, 2,
				Set.of(GENRE_THRILLER), MPA_G,
				Set.of(new Director(1, "director1"), new Director(2, "director2"))));
	}

	@Test
	public void getTopLikesWithYear() {
		List<Film> topLikes = filmDao.getTop(10, null, 1990);

		assertThat(topLikes).hasSize(1);
		assertThat(topLikes.get(0)).isEqualTo(new Film(1, "film1", "first test film",
				LocalDate.of(1990, 9, 10), 150, 1,
				Set.of(GENRE_COMEDY), MPA_PG, Collections.singleton(new Director(1, "director1"))));
	}

	@Test
	public void getTopLikesWithGenreIdAndYear() {
		List<Film> topLikes = filmDao.getTop(10, 4, 2005);

		assertThat(topLikes).hasSize(1);
		assertThat(topLikes.get(0)).isEqualTo(new Film(2, "film2", "second test film",
				LocalDate.of(2005, 12, 4), 120, 2,
				Set.of(GENRE_THRILLER), MPA_G,
				Set.of(new Director(1, "director1"), new Director(2, "director2"))));
	}

	@Test
	public void getAllFriendsById() {
		List<User> friends = friendsDao.getAllById(1);

		assertThat(friends).hasSize(2);
		assertThat(friends.get(0)).isEqualTo(new User(2, "2@yandex.ru", "user2", "second",
				LocalDate.of(1994, 10, 14)));
		assertThat(friends.get(1)).isEqualTo(new User(3, "3@yandex.ru", "user3", "third",
				LocalDate.of(1996, 6, 20)));
	}

	@Test
	public void getAllFriendsByIdWhenEmpty() {
		jdbcTemplate.update("DELETE FROM friends WHERE user_id = 1");
		List<User> friends = friendsDao.getAllById(1);

		assertThat(friends).hasSize(0);
	}

	@Test
	public void addFriend() {
		friendsDao.save(2, 1);
		List<User> friends = friendsDao.getAllById(2);

		assertThat(friends).hasSize(2);
		assertThat(friends.get(0)).isEqualTo(new User(3, "3@yandex.ru", "user3", "third",
				LocalDate.of(1996, 6, 20)));
		assertThat(friends.get(1)).isEqualTo(new User(1, "1@yandex.ru", "user1", "first",
				LocalDate.of(1992, 3, 4)));
	}

	@Test
	public void deleteFriend() {
		friendsDao.delete(1, 3);
		List<User> friends = friendsDao.getAllById(1);

		assertThat(friends).hasSize(1);
		assertThat(friends.get(0)).isEqualTo(new User(2, "2@yandex.ru", "user2", "second",
				LocalDate.of(1994, 10, 14)));
	}

	@Test
	public void getCommonById() {
		List<User> commonFriends = friendsDao.getCommonById(1, 2);

		assertThat(commonFriends).hasSize(1);
		assertThat(commonFriends.get(0)).isEqualTo(new User(3, "3@yandex.ru", "user3", "third",
				LocalDate.of(1996, 6, 20)));
	}

	@Test
	public void getCommonByIdWhenNoCommon() {
		List<User> commonFriends = friendsDao.getCommonById(2, 3);

		assertThat(commonFriends).hasSize(0);
	}

	@Test
	public void getCommonByIdWhenEmpty() {
		jdbcTemplate.update("DELETE FROM friends");
		List<User> commonFriends = friendsDao.getCommonById(1, 2);

		assertThat(commonFriends).hasSize(0);
	}

	@Test
	public void getAllDirectors() {
		List<Director> directors = directorDao.getAll();

		assertThat(directors).hasSize(2);
		assertThat(directors.get(0)).isEqualTo(new Director(1, "director1"));
		assertThat(directors.get(1)).isEqualTo(new Director(2, "director2"));
	}

	@Test
	public void getAllDirectorsWhenEmpty() {
		jdbcTemplate.update("DELETE FROM directors");
		List<Director> directors = directorDao.getAll();

		assertThat(directors).hasSize(0);
	}

	@Test
	public void getDirectorById() {
		Director director = directorDao.getById(1);

		assertThat(director).isNotNull();
		assertThat(director).hasFieldOrPropertyWithValue("id", 1);
	}

	@Test
	public void getDirectorByNonExistingId() {
		assertThatThrownBy(() -> directorDao.getById(999)).hasMessage("Режиссер с id=999 не найден.");
	}

	@Test
	public void addDirector() {
		Director director = new Director(0, "director3");
		Director directorReturned = directorDao.save(director);
		Director directorFromDb = directorDao.getById(3);

		assertThat(directorReturned).isNotNull();
		assertThat(directorReturned).hasFieldOrPropertyWithValue("id", 3);
		assertThat(directorFromDb).isNotNull();
		assertThat(directorFromDb).isEqualTo(directorReturned);
		assertThat(directorFromDb).hasFieldOrPropertyWithValue("name", "director3");
	}

	@Test
	public void updateDirector() {
		Director updatedDirector = new Director(1, "updated director1");
		Director directorReturned = directorDao.update(updatedDirector);
		Director directorFromDb = directorDao.getById(1);

		assertThat(directorReturned).isNotNull();
		assertThat(directorReturned).hasFieldOrPropertyWithValue("id", 1);
		assertThat(directorReturned).hasFieldOrPropertyWithValue("name", "updated director1");
		assertThat(directorFromDb).isNotNull();
		assertThat(directorFromDb).isEqualTo(directorReturned);
		assertThat(directorFromDb).hasFieldOrPropertyWithValue("name", "updated director1");
	}

	@Test
	public void deleteDirector() {
		directorDao.delete(2);
		List<Director> directors = directorDao.getAll();

		assertThat(directors).hasSize(1);
		assertThat(directors.get(0)).hasFieldOrPropertyWithValue("id", 1);
		assertThatThrownBy(() -> directorDao.getById(2)).hasMessage("Режиссер с id=2 не найден.");
	}

	@Test
	public void getAllByFilmId() {
		List<Director> directors = new ArrayList<>(directorDao.getAllByFilmId(1));

		assertThat(directors).hasSize(1);
		assertThat(directors.get(0)).hasFieldOrPropertyWithValue("id", 1);
		assertThat(directors.get(0)).hasFieldOrPropertyWithValue("name", "director1");
	}

	@Test
	public void getAllByFilmIdWhenEmpty() {
		List<Director> directors = new ArrayList<>(directorDao.getAllByFilmId(3));

		assertThat(directors).hasSize(0);
	}

	@Test
	public void getDirectorFilmsWithNoSorting() {
		List<Film> films = filmDao.getDirectorFilms(1, null);

		assertThat(films).hasSize(2);
		assertThat(films).contains(new Film(1, "film1", "first test film",
				LocalDate.of(1990, 9, 10), 150, 1,
				Set.of(GENRE_COMEDY), MPA_PG, Collections.singleton(new Director(1, "director1"))));
		assertThat(films).contains(new Film(2, "film2", "second test film",
				LocalDate.of(2005, 12, 4), 120, 2,
				Set.of(GENRE_THRILLER), MPA_G,
				Set.of(new Director(1, "director1"), new Director(2, "director2"))));
	}

	@Test
	public void getDirectorFilmsWithSortingByYear() {
		List<Film> films = filmDao.getDirectorFilms(1, "year");

		assertThat(films).hasSize(2);
		assertThat(films.get(0)).isEqualTo(new Film(1, "film1", "first test film",
				LocalDate.of(1990, 9, 10), 150, 1,
				Set.of(GENRE_COMEDY), MPA_PG, Collections.singleton(new Director(1, "director1"))));
		assertThat(films.get(1)).isEqualTo(new Film(2, "film2", "second test film",
				LocalDate.of(2005, 12, 4), 120, 2,
				Set.of(GENRE_THRILLER), MPA_G,
				Set.of(new Director(1, "director1"), new Director(2, "director2"))));
	}

	@Test
	public void getDirectorFilmsWithSortingByLikes() {
		List<Film> films = filmDao.getDirectorFilms(1, "likes");

		assertThat(films).hasSize(2);
		assertThat(films.get(0)).isEqualTo(new Film(2, "film2", "second test film",
				LocalDate.of(2005, 12, 4), 120, 2,
				Set.of(GENRE_THRILLER), MPA_G,
				Set.of(new Director(1, "director1"), new Director(2, "director2"))));
		assertThat(films.get(1)).isEqualTo(new Film(1, "film1", "first test film",
				LocalDate.of(1990, 9, 10), 150, 1,
				Set.of(GENRE_COMEDY), MPA_PG, Collections.singleton(new Director(1, "director1"))));
	}

	@Test
	public void getDirectorFilmsWithUnknownSorting() {
		assertThatThrownBy(() -> filmDao.getDirectorFilms(1, "name"))
				.hasMessage("Параметр сортировки должен быть year или likes");
	}

	@Test
	public void getCommonFilms() {
		List<Film> commonFilms = filmDao.getCommonFilms(1, 2);

		assertThat(commonFilms).hasSize(2);
		assertThat(commonFilms.get(0)).isEqualTo(new Film(3, "film3", "third test film",
				LocalDate.of(2008, 10, 1), 180, 3,
				Set.of(GENRE_DRAMA, GENRE_THRILLER, GENRE_ACTION), MPA_R, Collections.emptySet()));
		assertThat(commonFilms.get(1)).isEqualTo(new Film(2, "film2", "second test film",
				LocalDate.of(2005, 12, 4), 120, 2,
				Set.of(GENRE_THRILLER), MPA_G,
				Set.of(new Director(1, "director1"), new Director(2, "director2"))));
	}

	@Test
	public void getCommonFilmsWhenEmpty() {
		jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) " +
				"VALUES ('4@yandex.ru', 'user4', 'fourth', '1994-01-22')");
		List<Film> commonFilms = filmDao.getCommonFilms(1, 4);

		assertThat(commonFilms).hasSize(0);
	}

	@Test
	public void searchByTitle() {
		List<Film> result = filmDao.search("1", "title");

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(new Film(1, "film1", "first test film",
				LocalDate.of(1990, 9, 10), 150, 1,
				Set.of(GENRE_COMEDY), MPA_PG, Collections.singleton(new Director(1, "director1"))));
	}

	@Test
	public void searchByDirector() {
		List<Film> result = filmDao.search("1", "director");

		assertThat(result).hasSize(2);
		assertThat(result.get(0)).isEqualTo(new Film(2, "film2", "second test film",
				LocalDate.of(2005, 12, 4), 120, 2,
				Set.of(GENRE_THRILLER), MPA_G,
				Set.of(new Director(1, "director1"), new Director(2, "director2"))));
		assertThat(result.get(1)).isEqualTo(new Film(1, "film1", "first test film",
				LocalDate.of(1990, 9, 10), 150, 1,
				Set.of(GENRE_COMEDY), MPA_PG, Collections.singleton(new Director(1, "director1"))));
	}

	@Test
	public void searchByDirectorAndTitle() {
		jdbcTemplate.update("DELETE FROM film_director WHERE film_id = 1");
		List<Film> result = filmDao.search("1", "director,title");

		assertThat(result).hasSize(2);
		assertThat(result.get(0)).isEqualTo(new Film(2, "film2", "second test film",
				LocalDate.of(2005, 12, 4), 120, 2,
				Set.of(GENRE_THRILLER), MPA_G,
				Set.of(new Director(1, "director1"), new Director(2, "director2"))));
		assertThat(result.get(1)).isEqualTo(new Film(1, "film1", "first test film",
				LocalDate.of(1990, 9, 10), 150, 1,
				Set.of(GENRE_COMEDY), MPA_PG, Collections.emptySet()));
	}

	@Test
	public void getRecommendations() {
		jdbcTemplate.update("INSERT INTO films (name, description, release_date, duration, mpa_id) " +
				"VALUES ('film4', 'fourth test film', '2000-10-15', 100, 3)");
		jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) " +
				"VALUES ('4@yandex.ru', 'user4', 'fourth', '1995-10-12')");
		jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (1, 4), (3, 4), (4, 3)");
		List<Film> recommendations = filmDao.getRecommendations(4);

		assertThat(recommendations).hasSize(2);
		assertThat(recommendations.get(0)).isEqualTo(new Film(4, "film4", "fourth test film",
				LocalDate.of(2000, 10, 15), 100, 1,
				Collections.emptySet(), MPA_PG_13, Collections.emptySet()));
		assertThat(recommendations.get(1)).isEqualTo(new Film(2, "film2", "second test film",
				LocalDate.of(2005, 12, 4), 120, 2,
				Set.of(GENRE_THRILLER), MPA_G,
				Set.of(new Director(1, "director1"), new Director(2, "director2"))));
	}

	@Test
	public void getRecommendationsWhenNoLikes() {
		jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) " +
				"VALUES ('4@yandex.ru', 'user4', 'fourth', '1995-10-12')");
		List<Film> recommendations = filmDao.getRecommendations(4);

		assertThat(recommendations).hasSize(0);
	}

	@Test
	public void getRecommendationsWhenNoCommonLikes() {
		jdbcTemplate.update("DELETE FROM likes WHERE film_id = 1");
		jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) " +
				"VALUES ('4@yandex.ru', 'user4', 'fourth', '1995-10-12')");
		jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (1, 4)");
		List<Film> recommendations = filmDao.getRecommendations(4);

		assertThat(recommendations).hasSize(0);
	}

	@Test
	public void getAllReviews() {
		List<Review> reviews = reviewDao.getAll(null, 10);

		assertThat(reviews).hasSize(3);
		assertThat(reviews.get(0)).isEqualTo(new Review(1, "very good film", true, 1, 2, -1));
		assertThat(reviews.get(1)).isEqualTo(new Review(2, "the film is too bad", false, 2, 2, 0));
		assertThat(reviews.get(2)).isEqualTo(new Review(3, "the film is nice", true, 3, 3, 2));
	}

	@Test
	public void getAllReviewsWhenEmpty() {
		jdbcTemplate.update("DELETE FROM reviews");
		List<Review> reviews = reviewDao.getAll(null, 10);

		assertThat(reviews).hasSize(0);
	}

	@Test
	public void getAllReviewsWithSmallCount() {
		List<Review> reviews = reviewDao.getAll(null, 2);

		assertThat(reviews).hasSize(2);
		assertThat(reviews.get(0)).isEqualTo(new Review(1, "very good film", true, 1, 2, -1));
		assertThat(reviews.get(1)).isEqualTo(new Review(2, "the film is too bad", false, 2, 2, 0));
	}

	@Test
	public void getAllReviewsWithFilmId() {
		List<Review> reviews = reviewDao.getAll(2, 10);

		assertThat(reviews).hasSize(2);
		assertThat(reviews.get(0)).isEqualTo(new Review(1, "very good film", true, 1, 2, -1));
		assertThat(reviews.get(1)).isEqualTo(new Review(2, "the film is too bad", false, 2, 2, 0));
	}

	@Test
	public void getReviewById() {
		Review review = reviewDao.getById(1);

		assertThat(review).isNotNull();
		assertThat(review).hasFieldOrPropertyWithValue("reviewId", 1);
	}

	@Test
	public void getReviewByNonExistingId() {
		assertThatThrownBy(() -> reviewDao.getById(999)).hasMessage("Обзор с id=999 не найден.");
	}

    @Test
	public void addReview() {
		Review review = new Review("bad film", false, 1, 1);
		Review reviewReturned = reviewDao.save(review);
		Review reviewFromDb = reviewDao.getById(4);

		assertThat(reviewReturned).isNotNull();
		assertThat(reviewReturned).hasFieldOrPropertyWithValue("reviewId", 4);
		assertThat(reviewReturned).hasFieldOrPropertyWithValue("useful", 0);
		assertThat(reviewFromDb).isNotNull();
		assertThat(reviewFromDb).isEqualTo(reviewReturned);
		assertThat(reviewFromDb).hasFieldOrPropertyWithValue("reviewId", 4);
		assertThat(reviewFromDb).hasFieldOrPropertyWithValue("content", "bad film");
		assertThat(reviewFromDb).hasFieldOrPropertyWithValue("isPositive", false);
		assertThat(reviewFromDb).hasFieldOrPropertyWithValue("userId", 1);
		assertThat(reviewFromDb).hasFieldOrPropertyWithValue("filmId", 1);
		assertThat(reviewFromDb).hasFieldOrPropertyWithValue("useful", 0);
	}

	@Test
	public void updateReview() {
		Review updateReview = new Review(1, "not very good film", false, 1, 2, 0);
		Review reviewReturned = reviewDao.update(updateReview);
		Review reviewFromDb = reviewDao.getById(1);

		assertThat(reviewReturned).isNotNull();
		assertThat(reviewReturned).hasFieldOrPropertyWithValue("reviewId", 1);
		assertThat(reviewReturned).hasFieldOrPropertyWithValue("content", "not very good film");
		assertThat(reviewReturned).hasFieldOrPropertyWithValue("isPositive", false);
		assertThat(reviewReturned).hasFieldOrPropertyWithValue("userId", 1);
		assertThat(reviewReturned).hasFieldOrPropertyWithValue("filmId", 2);
		assertThat(reviewReturned).hasFieldOrPropertyWithValue("useful", -1);
		assertThat(reviewFromDb).isNotNull();
		assertThat(reviewFromDb).isEqualTo(reviewReturned);
		assertThat(reviewFromDb).hasFieldOrPropertyWithValue("reviewId", 1);
		assertThat(reviewFromDb).hasFieldOrPropertyWithValue("content", "not very good film");
		assertThat(reviewFromDb).hasFieldOrPropertyWithValue("isPositive", false);
		assertThat(reviewFromDb).hasFieldOrPropertyWithValue("userId", 1);
		assertThat(reviewFromDb).hasFieldOrPropertyWithValue("filmId", 2);
		assertThat(reviewFromDb).hasFieldOrPropertyWithValue("useful", -1);
	}

	@Test
	public void deleteReview() {
		reviewDao.delete(1);
		List<Review> reviews = reviewDao.getAll(null, 10);

		assertThatThrownBy(() -> reviewDao.getById(1)).hasMessage("Обзор с id=1 не найден.");
		assertThat(reviews).hasSize(2);
	}

	@Test
	public void addReviewLike() {
		reviewDao.addLike(2, 3);
		Review review = reviewDao.getById(2);

		assertThat(review).hasFieldOrPropertyWithValue("reviewId", 2);
		assertThat(review).hasFieldOrPropertyWithValue("useful", 1);
	}

	@Test
	public void addReviewDislike() {
		reviewDao.addDislike(2, 3);
		Review review = reviewDao.getById(2);

		assertThat(review).hasFieldOrPropertyWithValue("reviewId", 2);
		assertThat(review).hasFieldOrPropertyWithValue("useful", -1);
	}

	@Test
	public void deleteReviewLikeOrDislike() {
		reviewDao.deleteLikeOrDislike(1, 2);
		reviewDao.deleteLikeOrDislike(3, 1);
		Review review1 = reviewDao.getById(1);
		Review review3 = reviewDao.getById(3);

		assertThat(review1).hasFieldOrPropertyWithValue("reviewId", 1);
		assertThat(review1).hasFieldOrPropertyWithValue("useful", 0);
		assertThat(review3).hasFieldOrPropertyWithValue("reviewId", 3);
		assertThat(review3).hasFieldOrPropertyWithValue("useful", 1);
	}

	@Test
	public void writeEvent() {
		eventDao.writeEvent(1, EventType.REVIEW, Operation.ADD, 3);
		List<Event> events = eventDao.getUserEvents(1);

		assertThat(events).hasSize(1);
		assertThat(events.get(0)).hasFieldOrPropertyWithValue("eventId", 1);
		assertThat(events.get(0)).hasFieldOrPropertyWithValue("userId", 1);
		assertThat(events.get(0)).hasFieldOrPropertyWithValue("eventType", EventType.REVIEW);
		assertThat(events.get(0)).hasFieldOrPropertyWithValue("operation", Operation.ADD);
		assertThat(events.get(0)).hasFieldOrPropertyWithValue("entityId", 3);
		assertThat(events.get(0).getTimestamp()).isNotNull();
	}

	@Test
	public void getUserEvents() {
		eventDao.writeEvent(1, EventType.REVIEW, Operation.ADD, 3);
		eventDao.writeEvent(2, EventType.REVIEW, Operation.ADD, 3);
		eventDao.writeEvent(1, EventType.FRIEND, Operation.ADD, 2);
		List<Event> events = eventDao.getUserEvents(1);

		assertThat(events).hasSize(2);
		assertThat(events.get(0)).hasFieldOrPropertyWithValue("eventId", 1);
		assertThat(events.get(1)).hasFieldOrPropertyWithValue("eventId", 3);
	}
}