# java-filmorate

## Диаграмма базы данных приложения Filmorate
![dm-diagram.jpg](main%2Fresources%2Fdb%2Fdm-diagram.jpg)

Диаграмма состоит из таблиц:
- users *содержит инфо о  пользователях*
  - friendships   *о дружбе пользователей*
- films инфо   *о фильмах*
  - likes  *о лайках фильмов пользователями*
  - genre  *о жанрах фильмов*
  - film_genre *реализует связь многие ко многим для фильмов и жанров*
  - mpa_ratings *о рейтинге фильмов*

Некоторые примеры SQL:

Выбрать все фильмы, который лайкнул пользователь с user_id = 1
```SQL
SELECT f.*
FROM users u 
JOIN likes l ON u.user_id = l.user_Id
JOIN films f  ON l.film_id = f.film_id
WHERE u.user_id = 1
```

Узнать какие жанры у фильма с film_id = 1
```SQL
SELECT g.name
FROM films f 
JOIN genre g ON f.genre_id = g.genre_id
WHERE f.film_id = 1
```

Узнать с кем дружит пользователь c user_id = 1
```SQL
SELECT case when f.user_id = 1 then f.frind_id
            else f.user_id
       end friend_id,
       f.status    
FROM friendships f 
WHERE (f.user_id = 1 or f.frind_id = 1)
```
