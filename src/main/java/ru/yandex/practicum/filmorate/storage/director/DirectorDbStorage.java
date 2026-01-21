package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    // --- SQL Константы ---
    private static final String SQL_INSERT_DIRECTOR =
            "INSERT INTO directors (name) VALUES (?)";

    private static final String SQL_UPDATE_DIRECTOR =
            "UPDATE directors SET name = ? WHERE id = ?";

    private static final String SQL_SELECT_DIRECTOR_BY_ID =
            "SELECT id, name FROM directors WHERE id = ?";

    private static final String SQL_SELECT_ALL_DIRECTORS =
            "SELECT id, name FROM directors ORDER BY id";

    private static final String SQL_DELETE_DIRECTOR_BY_ID =
            "DELETE FROM directors WHERE id = ?";

    private static final String SQL_EXISTS_DIRECTOR_BY_ID =
            "SELECT EXISTS(SELECT 1 FROM directors WHERE id = ?)";

    // --- Методы реализации интерфейса ---

    @Override
    public Director create(Director director) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT_DIRECTOR, new String[]{"id"});
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);
        director.setId(keyHolder.getKey().longValue());
        return director;
    }

    @Override
    public Director update(Director director) {
        jdbcTemplate.update(SQL_UPDATE_DIRECTOR, director.getName(), director.getId());
        return director;
    }

    @Override
    public Optional<Director> getById(Long id) {
        List<Director> directors = jdbcTemplate.query(SQL_SELECT_DIRECTOR_BY_ID,
                (rs, rowNum) -> new Director(rs.getLong("id"), rs.getString("name")), id);
        return directors.stream().findFirst();
    }

    @Override
    public List<Director> getAll() {
        return jdbcTemplate.query(SQL_SELECT_ALL_DIRECTORS,
                (rs, rowNum) -> new Director(rs.getLong("id"), rs.getString("name")));
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(SQL_DELETE_DIRECTOR_BY_ID, id);
    }
}
