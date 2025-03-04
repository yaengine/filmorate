# java-filmorate

## Диаграмма базы данных приложения Filmorate
![Диаграмма базы данных приложения Filmorate](src%2Fmain%2Fresources%2Fdm-diagram.jpg "Диаграмма базы данных приложения Filmorate")

Диаграмма состоит из четырех таблиц:
- user *содержит инфо о  пользователях*
    - friendship   *о дружбе пользователей*
- film инфо   *о фильмах*
    - likes  *о лайках фильмов пользователями*
    - genre  *о жанрах фильмов*

Некоторые примеры SQL:

Выбрать все фильмы, который лайкнул пользователь с user_id = 1
```SQL
SELECT f.*
FROM user u 
JOIN likes l ON u.user_id = l.user_Id
JOIN film f  ON l.film_id = f.film_id
WHERE u.user_id = 1
```

Узнать какой жанр у фильма с film_id = 1
```SQL
SELECT g.name
FROM film f 
JOIN genre g ON f.genre_id = g.genre_id
WHERE f.film_id = 1
```

Узнать с кем дружит пользователь c user_id = 1
```SQL
SELECT case when f.user_id = 1 then f.frind_id
            else f.user_id
       end friend_id     
FROM friendship f 
WHERE (f.user_id = 1 or f.frind_id = 1)
```
