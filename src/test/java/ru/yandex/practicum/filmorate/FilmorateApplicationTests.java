package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FrendIdsRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserStorage.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class, FilmDbStorage.class, FilmRowMapper.class, FrendIdsRowMapper.class})
class FilmoRateApplicationTests {
	@Autowired
	private UserDbStorage userStorage;
	@Autowired
	private FilmDbStorage filmStorage;

    @Test
	public void testFindUserById() {
		Optional<User> userOptional = Optional.of(userStorage.findUserById(1));

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(user ->
						assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
				);
	}

	@Test
	public void testCreateUser() {
		User newUser = User.builder()
				.name("UserName")
				.email("user@email.com")
				.birthday(LocalDate.parse("2001-10-05"))
				.login("UserLogin")
				.friends(new HashSet<>())
				.build();
		newUser = userStorage.create(newUser);
		Optional<User> userOptional = Optional.of(userStorage.findUserById(newUser.getId()));

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(user ->
						assertThat(user).hasFieldOrPropertyWithValue("email", "user@email.com")
				);
	}

	@Test
	public void testFindAllUsers() {
		Optional<Collection<User>> userOptional = Optional.of(userStorage.findAll());

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(userList ->
						assertThat(userList.size()).isGreaterThan(0)
				);
	}

	@Test
	public void testUpdateUser() {
		User updUser = User.builder()
				.id(1L)
				.name("UserName")
				.email("user@email.com")
				.birthday(LocalDate.parse("2001-10-05"))
				.login("UserLogin")
				.friends(new HashSet<>())
				.build();
		updUser = userStorage.update(updUser);
		Optional<User> userOptional = Optional.of(userStorage.findUserById(updUser.getId()));

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(user ->
						assertThat(user).hasFieldOrPropertyWithValue("email", "user@email.com")
				);
	}

	@Test
	public void testFindFilmById() {
		Optional<Film> filmOptional = Optional.of(filmStorage.findFilmById(1));

		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(film ->
						assertThat(film).hasFieldOrPropertyWithValue("id", 1L)
				);
	}

	@Test
	public void testCreateFilm() {
		Film newFilm = Film.builder()
				.id(1L)
				.name("FilmName")
				.description("FilmDescr")
				.releaseDate(LocalDate.parse("2001-10-05"))
				.duration(100)
				.likes(new HashSet<>())
				.build();
		newFilm = filmStorage.create(newFilm);
		Optional<Film> filmOptional = Optional.of(filmStorage.findFilmById(newFilm.getId()));

		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(film ->
						assertThat(film).hasFieldOrPropertyWithValue("name", "FilmName")
				);
	}

	@Test
	public void testFindAllFilms() {
		Optional<Collection<Film>> filmOptional = Optional.of(filmStorage.findAll());

		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(filmList ->
						assertThat(filmList.size()).isGreaterThan(0)
				);
	}

	@Test
	public void testUpdateFilm() {
		Film updFilm = Film.builder()
				.id(1L)
				.name("FilmName")
				.description("FilmDescr")
				.releaseDate(LocalDate.parse("2001-10-05"))
				.duration(100)
				.likes(new HashSet<>())
				.build();
		updFilm = filmStorage.update(updFilm);
		Optional<Film> filmOptional = Optional.of(filmStorage.findFilmById(updFilm.getId()));

		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(film ->
						assertThat(film).hasFieldOrPropertyWithValue("name", "FilmName")
				);
	}
}
