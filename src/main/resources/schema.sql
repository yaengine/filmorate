DROP TABLE IF EXISTS PUBLIC.USERS CASCADE;
DROP TABLE IF EXISTS PUBLIC.FRIENDSHIPS CASCADE;
DROP TABLE IF EXISTS PUBLIC.FILMS CASCADE;
DROP TABLE IF EXISTS PUBLIC.MPA_RATINGS CASCADE;
DROP TABLE IF EXISTS PUBLIC.GENRES CASCADE;
DROP TABLE IF EXISTS PUBLIC.DIRECTORS CASCADE;
DROP TABLE IF EXISTS PUBLIC.LIKES CASCADE;
DROP TABLE IF EXISTS PUBLIC.FILM_GENRE CASCADE;
DROP TABLE IF EXISTS PUBLIC.REVIEWS CASCADE;
DROP TABLE IF EXISTS PUBLIC.USEFUL CASCADE;
DROP TABLE IF EXISTS PUBLIC.FILM_DIRECTORS CASCADE;
DROP TABLE IF EXISTS PUBLIC.FEED CASCADE;

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

CREATE TABLE IF NOT EXISTS PUBLIC.REVIEWS (
    REVIEW_ID INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    CONTENT VARCHAR,
    IS_POSITIVE BOOLEAN,
    USER_ID INT NOT NULL,
    FILM_ID INT NOT NULL,
    FOREIGN KEY (USER_ID) REFERENCES PUBLIC.USERS (USER_ID) ON DELETE CASCADE,
    FOREIGN KEY (FILM_ID) REFERENCES PUBLIC.FILMS (FILM_ID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS PUBLIC.USEFUL (
    REVIEW_ID INT NOT NULL,
    USER_ID INT NOT NULL,
    IS_LIKE BOOLEAN,
    FOREIGN KEY(REVIEW_ID) REFERENCES PUBLIC.REVIEWS(REVIEW_ID),
    FOREIGN KEY(USER_ID) REFERENCES PUBLIC.USERS(USER_ID)
);

CREATE TABLE IF NOT EXISTS PUBLIC.FEED (
    EVENT_ID INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    USER_ID INT NOT NULL,
    ENTITY_ID INT NOT NULL,
    TIME_STAMP BIGINT NOT NULL,
    EVENT_TYPE VARCHAR NOT NULL,
    OPERATION VARCHAR NOT NULL,
    FOREIGN KEY(USER_ID) REFERENCES PUBLIC.USERS(USER_ID) ON DELETE CASCADE,
    CONSTRAINT CHK_EVENT_TYPE CHECK (EVENT_TYPE IN ('LIKE', 'REVIEW', 'FRIEND')),
    CONSTRAINT CHK_OPERATION CHECK (OPERATION IN ('ADD', 'UPDATE', 'REMOVE'))
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

CREATE TABLE IF NOT EXISTS PUBLIC.DIRECTORS (
	DIRECTOR_ID BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    DIRECTOR_NAME VARCHAR(255) NOT NULL,
	CONSTRAINT DIRECTORS_UNIQUE UNIQUE (DIRECTOR_NAME)
);

CREATE TABLE IF NOT EXISTS PUBLIC.FILM_DIRECTORS (
	FILM_ID BIGINT NOT NULL,
    DIRECTOR_ID BIGINT NOT NULL,
    CONSTRAINT FILM_DIRECTORS_FILMS_FK FOREIGN KEY (FILM_ID) REFERENCES PUBLIC.FILMS(FILM_ID) ON DELETE CASCADE,
    CONSTRAINT FILM_DIRECTORS_DIRECTORS_FK FOREIGN KEY (DIRECTOR_ID) REFERENCES PUBLIC.DIRECTORS(DIRECTOR_ID) ON DELETE CASCADE,
    CONSTRAINT FILM_DIRECTORS_UNIQUE UNIQUE (FILM_ID, DIRECTOR_ID)
);

