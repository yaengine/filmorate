package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.directory.DirectoryStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.*;
import ru.yandex.practicum.filmorate.storage.user.UserStorage.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class, FilmDbStorage.class, FilmLikesRowMapper.class,
        DirectoryStorage.class, GenreRowMapper.class, MpaRowMapper.class})
public class FilmorateLikesTest {
    @Autowired
    private UserDbStorage userStorage;
    @Autowired
    private FilmDbStorage filmStorage;
    @Autowired
    private DirectoryStorage directoryStorage;

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
}
