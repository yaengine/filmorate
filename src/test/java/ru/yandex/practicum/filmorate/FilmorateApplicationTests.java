package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.directory.DirectoryStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.*;
import ru.yandex.practicum.filmorate.storage.user.UserStorage.UserDbStorage;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class, DirectoryStorage.class,
		UserRowMapper.class, FilmRowMapper.class, GenreRowMapper.class, MpaRowMapper.class,
		FrendIdsRowMapper.class, FilmLikesRowMapper.class
		 })
@ComponentScan("ru.yandex.practicum.filmorate")
class FilmorateApplicationTests {
	@Autowired
	private UserDbStorage userStorage;
	@Autowired
	private FilmDbStorage filmStorage;
	@Autowired
	private DirectoryStorage directoryStorage;

    @Test
	public void testFindUserById() {
		User newUser = User.builder()
				.name("UserName")
				.email("user@email.com")
				.birthday(LocalDate.parse("2001-10-05"))
				.login("UserLogin")
				.friends(new HashSet<>())
				.build();
		newUser = userStorage.create(newUser);

		Long userId = newUser.getId();
		Optional<User> userOptional = Optional.of(userStorage.findUserById(userId));

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(user ->
						assertThat(user).hasFieldOrPropertyWithValue("id", userId)
				);
	}

	@Test
	public void testCreateUser() {
		User newUser = User.builder()
				.name("User1Name")
				.email("user1@email.com")
				.birthday(LocalDate.parse("2001-10-05"))
				.login("User1Login")
				.friends(new HashSet<>())
				.build();
		newUser = userStorage.create(newUser);
		Optional<User> userOptional = Optional.of(userStorage.findUserById(newUser.getId()));

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(user ->
						assertThat(user).hasFieldOrPropertyWithValue("email", "user1@email.com")
				);
	}

	@Test
	public void testFindAllUsers() {
		User newUser = User.builder()
				.name("User2Name")
				.email("user2@email.com")
				.birthday(LocalDate.parse("2001-10-05"))
				.login("User2Login")
				.friends(new HashSet<>())
				.build();
		userStorage.create(newUser);
		Optional<Collection<User>> userOptional = Optional.of(userStorage.findAll());

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(userList ->
						assertThat(userList.size()).isGreaterThan(0)
				);
	}

	@Test
	public void testUpdateUser() {
		User newUser = User.builder()
				.name("User3Name")
				.email("user3@email.com")
				.birthday(LocalDate.parse("2001-10-05"))
				.login("User3Login")
				.friends(new HashSet<>())
				.build();
		newUser = userStorage.create(newUser);

		User updUser = User.builder()
				.id(newUser.getId())
				.name("User4Name")
				.email("user4@email.com")
				.birthday(LocalDate.parse("2001-10-05"))
				.login("User4Login")
				.friends(new HashSet<>())
				.build();
		updUser = userStorage.update(updUser);
		Optional<User> userOptional = Optional.of(userStorage.findUserById(updUser.getId()));

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(user ->
						assertThat(user).hasFieldOrPropertyWithValue("email", "user4@email.com")
				);
	}

	@Test
	public void testAddFriendUser() {
		User updUser = User.builder()
				.name("UserName11")
				.email("user11@email.com")
				.birthday(LocalDate.parse("2001-10-05"))
				.login("User11Login")
				.friends(new HashSet<>())
				.build();
		User updUser2 = User.builder()
				.name("User22Name")
				.email("user22@email.com")
				.birthday(LocalDate.parse("2001-10-05"))
				.login("User22Login")
				.friends(new HashSet<>())
				.build();
		updUser = userStorage.create(updUser);
		updUser2 = userStorage.create(updUser2);
		userStorage.addFriend(updUser.getId(), updUser2.getId());

		Optional<User> userOptional = Optional.of(userStorage.findUserById(updUser.getId()));

		Long user2Id = updUser2.getId();
		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(user -> {
					assertThat(user.getFriends()).containsExactly(user2Id);
				});
	}

	@Test
	public void testRemoveFriendUser() {
		User updUser = User.builder()
				.name("UserName5")
				.email("user5@email.com")
				.birthday(LocalDate.parse("2001-10-05"))
				.login("User5Login")
				.friends(new HashSet<>())
				.build();
		User updUser2 = User.builder()
				.name("User25Name")
				.email("user25@email.com")
				.birthday(LocalDate.parse("2001-10-05"))
				.login("User25Login")
				.friends(new HashSet<>())
				.build();
		updUser = userStorage.create(updUser);
		updUser2 = userStorage.create(updUser2);
		userStorage.addFriend(updUser.getId(), updUser2.getId());
		userStorage.removeFriend(updUser.getId(), updUser2.getId());

		Optional<User> userOptional = Optional.of(userStorage.findUserById(updUser.getId()));

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(user ->
						assertThat(user.getFriends())
								.isEmpty());
	}

	@Test
	public void testFindFilmById() {
		Film newFilm = Film.builder()
				.name("FilmName")
				.description("FilmDescr")
				.releaseDate(LocalDate.parse("2001-10-05"))
				.duration(100)
				.mpa(Mpa.builder().id(1L).build())
				.likes(new HashSet<>())
				.build();
		newFilm = filmStorage.create(newFilm);
		Long filmId = newFilm.getId();
		Optional<Film> filmOptional = Optional.of(filmStorage.findFilmById(filmId));

		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(film ->
						assertThat(film).hasFieldOrPropertyWithValue("id", filmId)
				);
	}

	@Test
	public void testCreateFilm() {
		Film newFilm = Film.builder()
				.name("FilmName")
				.description("FilmDescr")
				.releaseDate(LocalDate.parse("2001-10-05"))
				.duration(100)
				.mpa(Mpa.builder().id(1L).build())
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
		Film newFilm = Film.builder()
				.name("FilmName")
				.description("FilmDescr")
				.releaseDate(LocalDate.parse("2001-10-05"))
				.duration(100)
				.mpa(Mpa.builder().id(1L).build())
				.likes(new HashSet<>())
				.build();
		filmStorage.create(newFilm);

		Optional<Collection<Film>> filmOptional = Optional.of(filmStorage.findAll());

		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(filmList ->
						assertThat(filmList.size()).isGreaterThan(0)
				);
	}

	@Test
	public void testUpdateFilm() {
		Film newFilm = Film.builder()
				.name("FilmName")
				.description("FilmDescr")
				.releaseDate(LocalDate.parse("2001-10-05"))
				.duration(100)
				.mpa(Mpa.builder().id(1L).build())
				.likes(new HashSet<>())
				.build();
		newFilm = filmStorage.create(newFilm);
		Long filmId = newFilm.getId();
		Film updFilm = Film.builder()
				.id(filmId)
				.name("Film1Name")
				.description("Film1Descr")
				.releaseDate(LocalDate.parse("2001-10-05"))
				.duration(100)
				.likes(new HashSet<>())
				.mpa(Mpa.builder().id(filmId).build())
				.build();
		updFilm = filmStorage.update(updFilm);
		Optional<Film> filmOptional = Optional.of(filmStorage.findFilmById(updFilm.getId()));

		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(film ->
						assertThat(film).hasFieldOrPropertyWithValue("name", "Film1Name")
				);
	}

	@Test
	public void testAddFilmLike() {
		Film newFilm = Film.builder()
				.name("FilmName")
				.description("FilmDescr")
				.releaseDate(LocalDate.parse("2001-10-05"))
				.duration(100)
				.mpa(Mpa.builder().id(1L).build())
				.likes(new HashSet<>())
				.build();
		User updUser = User.builder()
				.name("UserName1")
				.email("user1@email.com")
				.birthday(LocalDate.parse("2001-10-05"))
				.login("User1Login")
				.friends(new HashSet<>())
				.build();
		updUser = userStorage.create(updUser);
		newFilm = filmStorage.create(newFilm);
		filmStorage.addLike(newFilm.getId(), updUser.getId());
		Optional<Film> filmOptional = Optional.of(filmStorage.findFilmById(newFilm.getId()));

		Long user2Id = updUser.getId();
		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(film -> {
					assertThat(film.getLikes()).containsExactly(user2Id);
				});
	}

	@Test
	public void testRemoveFilmLike() {
		Film newFilm = Film.builder()
				.name("FilmName")
				.description("FilmDescr")
				.releaseDate(LocalDate.parse("2001-10-05"))
				.duration(100)
				.mpa(Mpa.builder().id(1L).build())
				.likes(new HashSet<>())
				.build();
		User updUser = User.builder()
				.name("UserName1")
				.email("user1@email.com")
				.birthday(LocalDate.parse("2001-10-05"))
				.login("User1Login")
				.friends(new HashSet<>())
				.build();
		updUser = userStorage.create(updUser);
		newFilm = filmStorage.create(newFilm);
		filmStorage.addLike(newFilm.getId(), updUser.getId());
		filmStorage.removeLike(newFilm.getId(), updUser.getId());
		Optional<Film> filmOptional = Optional.of(filmStorage.findFilmById(newFilm.getId()));

		Long user2Id = updUser.getId();
		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(film -> {
					assertThat(film.getLikes()).isEmpty();
				});
	}

}
