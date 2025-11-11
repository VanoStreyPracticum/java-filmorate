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
import ru.yandex.practicum.filmorate.model.Director;
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

    // --- SQL для режиссёров ---
    private static final String SQL_INSERT_FILM_DIRECTOR = """
        MERGE INTO film_directors (film_id, director_id)
        KEY (film_id, director_id)
        VALUES (?, ?)
        """;

    private static final String SQL_SELECT_DIRECTORS_BY_FILM_ID = """
        SELECT d.id, d.name
        FROM film_directors fd
        JOIN directors d ON fd.director_id = d.id
        WHERE fd.film_id = ?
        ORDER BY d.id
        """;

    private static final String SQL_SELECT_FILMS_BY_DIRECTOR_SORT_YEAR = """
        SELECT f.id, f.name, f.description, f.release_date, f.duration,
               f.mpa_id, m.name AS mpa_name
        FROM films f
        JOIN film_directors fd ON f.id = fd.film_id
        LEFT JOIN mpa m ON f.mpa_id = m.id
        WHERE fd.director_id = ?
        ORDER BY f.release_date
        """;

    private static final String SQL_SELECT_FILMS_BY_DIRECTOR_SORT_LIKES = """
        SELECT f.id, f.name, f.description, f.release_date, f.duration,
               f.mpa_id, m.name AS mpa_name
        FROM films f
        JOIN film_directors fd ON f.id = fd.film_id
        LEFT JOIN likes l ON f.id = l.film_id
        LEFT JOIN mpa m ON f.mpa_id = m.id
        WHERE fd.director_id = ?
        GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name
        ORDER BY COUNT(l.user_id) DESC
        """;

    private static final String SQL_EXISTS_RATING = "SELECT COUNT(*) FROM mpa WHERE id = ?";
    private static final String SQL_EXISTS_GENRE = "SELECT COUNT(*) FROM genres WHERE id = ?";

    // --- Реализация методов ---
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
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT_FILM, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null);
            ps.setObject(4, film.getDuration());
            ps.setObject(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKeyAs(Long.class));

        saveFilmGenres(film);
        saveFilmDirectors(film);

        loadRelations(film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        requireFilmExists(film.getId());
        jdbcTemplate.update(SQL_UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        saveFilmGenres(film);
        saveFilmDirectors(film);
        loadRelations(film);
        return film;
    }

    @Override
    public Optional<Film> getFilm(long id) {
        List<Film> films = jdbcTemplate.query(SQL_SELECT_FILM_BY_ID, filmRowMapper, id);
        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.getFirst();
        loadRelations(film);
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
        films.forEach(this::loadRelations);
        return films;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update(SQL_INSERT_LIKE, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        jdbcTemplate.update(SQL_DELETE_LIKE, filmId, userId);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        List<Film> films = jdbcTemplate.query(SQL_SELECT_POPULAR_FILMS, filmRowMapper, count);
        films.forEach(this::loadRelations);
        return films;
    }

    @Override
    public Collection<Film> getFilmsByDirectorSortedByYear(int directorId) {
        List<Film> films = jdbcTemplate.query(SQL_SELECT_FILMS_BY_DIRECTOR_SORT_YEAR, filmRowMapper, directorId);
        films.forEach(this::loadRelations);
        return films;
    }

    @Override
    public Collection<Film> getFilmsByDirectorSortedByLikes(int directorId) {
        List<Film> films = jdbcTemplate.query(SQL_SELECT_FILMS_BY_DIRECTOR_SORT_LIKES, filmRowMapper, directorId);
        films.forEach(this::loadRelations);
        return films;
    }

    // --- Вспомогательные методы ---
    private void saveFilmGenres(Film film) {
        if (film.getGenres() == null) {
            return;
        }

        List<Genre> uniqueGenres = film.getGenres().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Genre::getId, g -> g, (a, b) -> a, LinkedHashMap::new),
                        map -> new ArrayList<>(map.values())
                ));

        jdbcTemplate.batchUpdate(SQL_MERGE_FILM_GENRES, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, film.getId());
                ps.setInt(2, uniqueGenres.get(i).getId());
            }

            public int getBatchSize() {
                return uniqueGenres.size();
            }
        });
    }

    private void saveFilmDirectors(Film film) {
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            return;
        }

        List<Director> directors = new ArrayList<>(film.getDirectors());
        jdbcTemplate.batchUpdate(SQL_INSERT_FILM_DIRECTOR, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, film.getId());
                ps.setInt(2, directors.get(i).getId());
            }

            public int getBatchSize() {
                return directors.size();
            }
        });
    }

    private void loadRelations(Film film) {
        film.setGenres(new LinkedHashSet<>(jdbcTemplate.query(SQL_SELECT_GENRES_BY_FILM_ID, genreRowMapper, film.getId())));
        film.setLikes(new LinkedHashSet<>(jdbcTemplate.queryForList(SQL_SELECT_LIKES_BY_FILM_ID, Long.class, film.getId())));
        film.setDirectors(new LinkedHashSet<>(jdbcTemplate.query(
                SQL_SELECT_DIRECTORS_BY_FILM_ID,
                (rs, rowNum) -> new Director(rs.getInt("id"), rs.getString("name")),
                film.getId()
        )));
    }

    private boolean ratingExists(Integer ratingId) {
        if (ratingId == null) {
            return true;
        }
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
