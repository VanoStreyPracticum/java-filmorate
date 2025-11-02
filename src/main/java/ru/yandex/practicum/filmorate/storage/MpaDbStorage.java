package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage {
    private final JdbcTemplate jdbc;
    private final MpaRowMapper mapper = new MpaRowMapper();

    public List<Mpa> findAll() {
        String sql = "SELECT * FROM mpa ORDER BY id";
        return jdbc.query(sql, mapper);
    }

    public Mpa findById(int id) {
        String sql = "SELECT * FROM mpa WHERE id = ?";
        List<Mpa> list = jdbc.query(sql, mapper, id);
        if (list.isEmpty()) {
            throw new NotFoundException("Mpa с id=" + id + " не найден");
        }
        return list.getFirst();
    }
}
