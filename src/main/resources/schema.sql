CREATE TABLE IF NOT EXISTS PUBLIC.USERS (
	USER_ID BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	USER_NAME VARCHAR(255),
	EMAIL VARCHAR(255),
	LOGIN VARCHAR(255),
	BIRTHDAY DATE
);

CREATE TABLE IF NOT EXISTS PUBLIC.MPA_RATINGS (
    MPA_RATING_ID BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    MPA_RATING_NAME VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS PUBLIC.FILMS (
	FILM_ID BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	FILM_NAME VARCHAR(255),
	DESCRIPTION VARCHAR(255),
	RELEASE_DATE DATE,
	DURATION INTEGER,
	MPA_RATING_ID BIGINT,
	CONSTRAINT FILMS_MPA_RATINGS_FK FOREIGN KEY (MPA_RATING_ID) REFERENCES PUBLIC.MPA_RATINGS(MPA_RATING_ID)
	ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS PUBLIC.FRIENDSHIPS (
	USER_ID BIGINT NOT NULL,
	FRIEND_ID BIGINT NOT NULL,
	STATUS BOOLEAN NOT NULL,
	CONSTRAINT FRIENDSHIPS_PK PRIMARY KEY (USER_ID, FRIEND_ID),
	CONSTRAINT FRIENDSHIPS_USERS_FK FOREIGN KEY (USER_ID) REFERENCES PUBLIC.USERS(USER_ID) ON DELETE CASCADE,
	CONSTRAINT FRIENDSHIPS_USERS_FK_1 FOREIGN KEY (FRIEND_ID) REFERENCES PUBLIC.USERS(USER_ID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS PUBLIC.LIKES (
	LIKE_ID BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	USER_ID BIGINT NOT NULL,
	FILM_ID BIGINT NOT NULL,
	CONSTRAINT LIKES_USERS_FK FOREIGN KEY (USER_ID) REFERENCES PUBLIC.USERS(USER_ID) ON DELETE CASCADE,
	CONSTRAINT LIKES_FILMS_FK FOREIGN KEY (FILM_ID) REFERENCES PUBLIC.FILMS(FILM_ID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS PUBLIC.GENRES (
	GENRE_ID BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	GENRE_NAME VARCHAR NOT NULL,
	CONSTRAINT GENRES_UNIQUE UNIQUE (GENRE_NAME)
);

CREATE TABLE IF NOT EXISTS PUBLIC.FILM_GENRE (
	FILM_ID BIGINT NOT NULL,
	GENRE_ID BIGINT NOT NULL,
	CONSTRAINT FILM_GENRE_FILMS_FK FOREIGN KEY (FILM_ID) REFERENCES PUBLIC.FILMS(FILM_ID) ON DELETE CASCADE,
	CONSTRAINT FILM_GENRE_GENRES_FK FOREIGN KEY (GENRE_ID) REFERENCES PUBLIC.GENRES(GENRE_ID) ON DELETE CASCADE,
	CONSTRAINT FILM_GENRE_UNIQUE UNIQUE (FILM_ID, GENRE_ID)
);


