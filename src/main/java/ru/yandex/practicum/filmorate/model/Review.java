package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Review {
    Long reviewId;
    String content;
    Long filmId;
    Long userId;
    Boolean isPositive;
    Integer useful;
}
