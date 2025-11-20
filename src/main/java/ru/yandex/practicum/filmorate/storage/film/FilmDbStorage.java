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

    public static final String SQL_DELETE_FILM_GENRES =
            "DELETE FROM film_genres WHERE film_id = ?";

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

    private static final String SQL_SELECT_USERS_WITH_SIMILAR_TASTES = """
            SELECT ul2.user_id
            FROM (SELECT *
                  FROM LIKES
                  WHERE USER_ID = ?) AS ul1
                     JOIN LIKES AS ul2 ON ul1.FILM_ID = ul2.FILM_ID
                AND ul1.user_id != ul2.user_id
            GROUP BY ul2.user_id, ul2.user_id
            ORDER BY COUNT(*) DESC;
            """;

    private static final String SQL_SELECT_RECOMMENDED_FILMS = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
                   f.mpa_id, m.name AS mpa_name
            FROM FILMS AS f
                     JOIN MPA AS m ON m.id = f.mpa_id
            WHERE f.id IN (SELECT ul1.FILM_ID
                         FROM (SELECT *
                               FROM LIKES
                               WHERE USER_ID = ?) AS ul1
                                  LEFT JOIN (SELECT *
                                             FROM LIKES
                                             WHERE USER_ID = ?) ul2 ON ul1.FILM_ID = ul2.FILM_ID
                         WHERE ul2.USER_ID IS NULL);
            """;

    // --- SQL для режиссёров ---
    private static final String SQL_INSERT_FILM_DIRECTOR = """
            MERGE INTO film_directors (film_id, director_id)
            KEY (film_id, director_id)
            VALUES (?, ?)
            """;

    public static final String SQL_DELETE_FILM_DIRECTORS =
            "DELETE FROM film_directors WHERE film_id = ?";

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

    private static final String SQL_SELECT_COMMON_FILMS = """
            SELECT f.id,
                f.name,
                f.description,
                f.release_date,
                f.duration,
                f.mpa_id,
                m.name AS mpa_name
            FROM films f
            JOIN mpa m ON f.mpa_id = m.id
            WHERE f.id IN (
                SELECT film_id FROM likes WHERE user_id = ?
            )
            AND f.id IN (
                SELECT film_id FROM likes WHERE user_id = ?
            )
            ORDER BY (
                SELECT COUNT(*) FROM likes l WHERE l.film_id = f.id
            ) DESC
            """;

    private static final String SQL_DELETE_FILM = """
            DELETE FROM films WHERE id = ?
            """;

    private static final String SQL_SELECT_POPULAR_FILTER = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
                   f.mpa_id, m.name AS mpa_name,
                   COUNT(l.user_id) AS likes_count
            FROM films f
            LEFT JOIN mpa m ON f.mpa_id = m.id
            LEFT JOIN likes l ON f.id = l.film_id
            """;

    private static final String SQL_JOIN_GENRE =
            "JOIN film_genres fg ON f.id = fg.film_id AND fg.genre_id = ?";

    private static final String SQL_WHERE_CLAUSE = "WHERE 1=1 ";

    private static final String SQL_FILTER_BY_YEAR =
            "AND EXTRACT(YEAR FROM f.release_date) = ?";

    private static final String SQL_GROUP_ORDER_LIMIT =
            "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT ?";

    private static final String SQL_SEARCH_BASE = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
                   f.mpa_id, m.name AS mpa_name
            FROM films f
            LEFT JOIN mpa m ON f.mpa_id = m.id
            LEFT JOIN film_directors fd ON f.id = fd.film_id
            LEFT JOIN directors d ON fd.director_id = d.id
            LEFT JOIN likes l ON f.id = l.film_id
            WHERE
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
        if (films.isEmpty()) return Optional.empty();
        Film film = films.getFirst();
        loadRelations(film);
        return Optional.of(film);
    }

    @Override
    public boolean existsFilm(long id) {
        return jdbcTemplate.queryForObject(SQL_EXISTS_FILM, Boolean.class, id);
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
    public Collection<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        StringBuilder sql = new StringBuilder(SQL_SELECT_POPULAR_FILTER);
        List<Object> params = new ArrayList<>();

        if (genreId != null) {
            sql.append(SQL_JOIN_GENRE);
            params.add(genreId);
        }

        sql.append(SQL_WHERE_CLAUSE);

        if (year != null) {
            sql.append(SQL_FILTER_BY_YEAR);
            params.add(year);
        }

        sql.append(SQL_GROUP_ORDER_LIMIT);
        params.add(count);

        List<Film> films = jdbcTemplate.query(sql.toString(), filmRowMapper, params.toArray());

        if (!films.isEmpty()) films.forEach(this::loadRelations);

        return films;
    }

    @Override
    public Collection<Film> getFilmsByDirectorSortedByYear(int directorId) {
        List<Film> films = jdbcTemplate.query(SQL_SELECT_FILMS_BY_DIRECTOR_SORT_YEAR, filmRowMapper, directorId);
        films.forEach(this::loadRelations);
        return films;
    }

    @Override
    public void deleteFilm(long id) {
        requireFilmExists(id);
        jdbcTemplate.update(SQL_DELETE_FILM, id);
    }

    @Override
    public Collection<Film> getCommonFilms(long userId, long friendId) {
        List<Film> films = jdbcTemplate.query(SQL_SELECT_COMMON_FILMS, filmRowMapper, userId, friendId);
        films.forEach(this::loadRelations);
        return films;
    }

    @Override
    public Collection<Film> searchFilms(String query, Collection<String> searchBy) {

        StringBuilder sql = new StringBuilder(SQL_SEARCH_BASE);
        List<Object> params = new ArrayList<>();

        List<String> conditions = new ArrayList<>();

        if (searchBy.contains("title")) {
            conditions.add("LOWER(f.name) LIKE ?");
            params.add("%" + query + "%");
        }

        if (searchBy.contains("director")) {
            conditions.add("LOWER(d.name) LIKE ?");
            params.add("%" + query + "%");
        }

        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("searchBy должен содержать title, director или оба");
        }

        sql.append(String.join(" OR ", conditions));

        sql.append("""
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name
                ORDER BY COUNT(l.user_id) DESC
                """);

        List<Film> films = jdbcTemplate.query(sql.toString(), filmRowMapper, params.toArray());
        films.forEach(this::loadRelations);
        return films;
    }


    // --- Вспомогательные методы ---
    @Override
    public Collection<Film> getFilmsByDirectorSortedByLikes(int directorId) {
        List<Film> films = jdbcTemplate.query(SQL_SELECT_FILMS_BY_DIRECTOR_SORT_LIKES, filmRowMapper, directorId);
        films.forEach(this::loadRelations);
        return films;
    }

    @Override
    public Collection<Film> getRecommendedFilms(int userId) {
        List<Integer> usersWithSimilarTastes = jdbcTemplate.queryForList(SQL_SELECT_USERS_WITH_SIMILAR_TASTES,
                Integer.class, userId);

        List<Film> recommendedFilms = new ArrayList<>();

        for (Integer id : usersWithSimilarTastes) {
            recommendedFilms = jdbcTemplate.query(con -> {
                var ps = con.prepareStatement(SQL_SELECT_RECOMMENDED_FILMS);
                ps.setInt(1, id);
                ps.setInt(2, userId);
                return ps;
            }, new FilmRowMapper());

            if (!recommendedFilms.isEmpty()) {
                break;
            }
        }

        recommendedFilms.stream().forEach(this::loadRelations);

        return recommendedFilms;
    }

    private void saveFilmGenres(Film film) {
        if (film.getGenres() == null) return;
        jdbcTemplate.update(SQL_DELETE_FILM_GENRES, film.getId());

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
        if (film.getDirectors() == null) return;
        jdbcTemplate.update(SQL_DELETE_FILM_DIRECTORS, film.getId());

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
        return count > 0;
    }

    private boolean genreExists(Integer genreId) {
        Integer count = jdbcTemplate.queryForObject(SQL_EXISTS_GENRE, Integer.class, genreId);
        return count > 0;
    }

    private void requireFilmExists(Long filmId) {
        if (!existsFilm(filmId)) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
    }
}
