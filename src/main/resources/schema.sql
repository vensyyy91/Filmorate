DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS review_like CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS friends CASCADE;
DROP TABLE IF EXISTS likes CASCADE;
DROP TABLE IF EXISTS marks CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS film_director CASCADE;
DROP TABLE IF EXISTS film_genre CASCADE;
DROP TABLE IF EXISTS films CASCADE;
DROP TABLE IF EXISTS directors CASCADE;
DROP TABLE IF EXISTS mpa CASCADE;
DROP TABLE IF EXISTS genres CASCADE;

CREATE TABLE IF NOT EXISTS genres (
	genre_id INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	genre_name VARCHAR(14) NOT NULL
);

CREATE TABLE IF NOT EXISTS mpa (
	mpa_id INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	mpa_name VARCHAR(5) NOT NULL
);

CREATE TABLE IF NOT EXISTS directors (
    director_id INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    director_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS films (
    id INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    release_date TIMESTAMP,
    duration INTEGER NOT NULL,
    mpa_id INTEGER REFERENCES mpa(mpa_id)
);

CREATE TABLE IF NOT EXISTS film_genre (
    film_id INTEGER NOT NULL REFERENCES films(id) ON DELETE CASCADE,
    genre_id INTEGER NOT NULL REFERENCES genres(genre_id)
);

CREATE TABLE IF NOT EXISTS film_director (
    film_id INTEGER NOT NULL REFERENCES films(id) ON DELETE CASCADE,
    director_id INTEGER NOT NULL REFERENCES directors(director_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS users (
    id INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    login VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR,
    birthday TIMESTAMP
);

CREATE TABLE IF NOT EXISTS likes (
    film_id INTEGER NOT NULL REFERENCES films(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS friends (
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    friend_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reviews (
    review_id INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    content VARCHAR NOT NULL,
    is_positive BOOLEAN NOT NULL,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    film_id INTEGER NOT NULL REFERENCES films(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS review_like (
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    review_id INTEGER NOT NULL REFERENCES reviews(review_id) ON DELETE CASCADE,
    is_positive BOOLEAN NOT NULL,
    CONSTRAINT uq_review_like UNIQUE (user_id, review_id)
);

CREATE TABLE IF NOT EXISTS events (
    event_id INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    create_timestamp TIMESTAMP NOT NULL,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_type VARCHAR(6) NOT NULL,
    operation VARCHAR(6) NOT NULL,
    entity_id INTEGER NOT NULL
);