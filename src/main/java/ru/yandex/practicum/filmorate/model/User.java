package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class User {
    Long id;
    String name;
    String email;
    String login;
    LocalDate birthday;
    Set<Long> friends;
}
