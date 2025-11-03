package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Repository
@Qualifier("genreDbStorage")
@RequiredArgsConstructor
public class GenreDbStorage {
    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper = new GenreRowMapper();

    public List<Genre> findAll() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbc.query(sql, mapper);
    }

    public Genre findById(int id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        List<Genre> list = jdbc.query(sql, mapper, id);
        if (list.isEmpty()) {
            throw new NotFoundException("Жанр с id=" + id + " не найден");
        }
        return list.getFirst();
    }
}
