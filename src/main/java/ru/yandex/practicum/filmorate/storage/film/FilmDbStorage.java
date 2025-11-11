package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper = new FilmRowMapper();
    private final GenreRowMapper genreRowMapper = new GenreRowMapper();

    // --- SQL запросы ---
    private static final String SQL_INSERT_FILM = """
        INSERT INTO films (name, description, release_date, duration, mpa_id)
        VALUES (?, ?, ?, ?, ?)
        """;

    private static final String SQL_UPDATE_FILM = """
        UPDATE films
        SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
        WHERE id = ?
        """;

    private static final String SQL_SELECT_FILM_BY_ID = """
        SELECT f.id, f.name, f.description, f.release_date, f.duration,
               f.mpa_id, m.name AS mpa_name
        FROM films f
        LEFT JOIN mpa m ON f.mpa_id = m.id
        WHERE f.id = ?
        """;

    private static final String SQL_SELECT_ALL_FILMS = """
        SELECT f.id, f.name, f.description, f.release_date, f.duration,
               f.mpa_id, m.name AS mpa_name
        FROM films f
        LEFT JOIN mpa m ON f.mpa_id = m.id
        """;

    private static final String SQL_EXISTS_FILM = """
        SELECT COUNT(*) > 0 FROM films WHERE id = ?
        """;

    private static final String SQL_INSERT_LIKE = """
        MERGE INTO likes (film_id, user_id)
        KEY (film_id, user_id)
        VALUES (?, ?)
        """;

    private static final String SQL_DELETE_LIKE = """
        DELETE FROM likes WHERE film_id = ? AND user_id = ?
        """;

    private static final String SQL_SELECT_POPULAR_FILMS = """
        SELECT f.id, f.name, f.description, f.release_date, f.duration,
               f.mpa_id, m.name AS mpa_name
        FROM films f
        LEFT JOIN mpa m ON f.mpa_id = m.id
        LEFT JOIN likes l ON f.id = l.film_id
        GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name
        ORDER BY COUNT(l.user_id) DESC
        LIMIT ?
        """;

    private static final String SQL_MERGE_FILM_GENRES = """
        MERGE INTO film_genres (film_id, genre_id)
        KEY (film_id, genre_id)
        VALUES (?, ?)
        """;

    private static final String SQL_SELECT_GENRES_BY_FILM_ID = """
        SELECT g.id, g.name
        FROM film_genres fg
        JOIN genres g ON fg.genre_id = g.id
        WHERE fg.film_id = ?
        ORDER BY g.id
        """;

    private static final String SQL_SELECT_LIKES_BY_FILM_ID = """
        SELECT user_id
        FROM likes
        WHERE film_id = ?
        """;

    private static final String SQL_EXISTS_RATING = """
        SELECT COUNT(*) FROM mpa WHERE id = ?
        """;

    private static final String SQL_EXISTS_GENRE = """
        SELECT COUNT(*) FROM genres WHERE id = ?
        """;

    // --- Новые запросы для удаления фильма ---
    private static final String SQL_DELETE_FILM_GENRES = """
        DELETE FROM film_genres WHERE film_id = ?
        """;

    private static final String SQL_DELETE_FILM_LIKES = """
        DELETE FROM likes WHERE film_id = ?
        """;

    private static final String SQL_DELETE_FILM = """
        DELETE FROM films WHERE id = ?
        """;

    // --- Методы реализации интерфейса ---
    @Override
    public Film addFilm(Film film) {
        if (film.getMpa() != null && !ratingExists(film.getMpa().getId())) {
            throw new NotFoundException("MPA с id=" + film.getMpa().getId() + " не найден");
        }

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                if (!genreExists(genre.getId())) {
                    throw new NotFoundException("Жанр с id=" + genre.getId() + " не найден");
                }
            }
        }

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(SQL_INSERT_FILM, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setDate(3, film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null);
            statement.setObject(4, film.getDuration());
            statement.setObject(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return statement;
        }, keyHolder);

        film.setId(keyHolder.getKeyAs(Long.class));
        saveFilmGenres(film);
        loadGenresAndLikes(film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        requireFilmExists(film.getId());

        jdbcTemplate.update(SQL_UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        return film;
    }

    @Override
    public Optional<Film> getFilm(long id) {
        List<Film> films = jdbcTemplate.query(SQL_SELECT_FILM_BY_ID, filmRowMapper, id);
        if (films.isEmpty()) return Optional.empty();
        Film film = films.getFirst();
        loadGenresAndLikes(film);
        return Optional.of(film);
    }

    @Override
    public boolean existsFilm(long id) {
        Boolean exists = jdbcTemplate.queryForObject(SQL_EXISTS_FILM, Boolean.class, id);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public Collection<Film> getAllFilms() {
        List<Film> films = jdbcTemplate.query(SQL_SELECT_ALL_FILMS, filmRowMapper);
        films.forEach(this::loadGenresAndLikes);
        return films;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update(SQL_INSERT_LIKE, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) { // исправлен порядок аргументов
        jdbcTemplate.update(SQL_DELETE_LIKE, filmId, userId);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        List<Film> films = jdbcTemplate.query(SQL_SELECT_POPULAR_FILMS, filmRowMapper, count);
        films.forEach(this::loadGenresAndLikes);
        return films;
    }

    @Override
    public void deleteFilm(long id) {
        requireFilmExists(id);
        jdbcTemplate.update(SQL_DELETE_FILM_GENRES, id);
        jdbcTemplate.update(SQL_DELETE_FILM_LIKES, id);
        jdbcTemplate.update(SQL_DELETE_FILM, id);
    }

    // --- Вспомогательные методы ---
    private void saveFilmGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) return;

        List<Genre> uniqueGenres = film.getGenres().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Genre::getId, g -> g, (a, b) -> a, LinkedHashMap::new),
                        map -> new ArrayList<>(map.values())
                ));

        jdbcTemplate.batchUpdate(SQL_MERGE_FILM_GENRES, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement statement, int i) throws SQLException {
                statement.setLong(1, film.getId());
                statement.setInt(2, uniqueGenres.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return uniqueGenres.size();
            }
        });
    }

    private void loadGenresAndLikes(Film film) {
        LinkedHashSet<Genre> genres = jdbcTemplate.query(SQL_SELECT_GENRES_BY_FILM_ID, genreRowMapper, film.getId())
                .stream()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        film.setGenres(genres);

        Set<Long> likes = new LinkedHashSet<>(jdbcTemplate.queryForList(SQL_SELECT_LIKES_BY_FILM_ID, Long.class, film.getId()));
        film.setLikes(likes);
    }

    private boolean ratingExists(Integer ratingId) {
        if (ratingId == null) return true;
        Integer count = jdbcTemplate.queryForObject(SQL_EXISTS_RATING, Integer.class, ratingId);
        return count != null && count > 0;
    }

    private boolean genreExists(Integer genreId) {
        Integer count = jdbcTemplate.queryForObject(SQL_EXISTS_GENRE, Integer.class, genreId);
        return count != null && count > 0;
    }

    private void requireFilmExists(Long filmId) {
        if (!existsFilm(filmId)) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
    }
}
