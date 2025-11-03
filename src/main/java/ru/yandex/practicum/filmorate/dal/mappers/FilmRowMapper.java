package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;

public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();

        film.setId(resultSet.getLong("id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));

        Date releaseDate = resultSet.getDate("release_date");
        if (releaseDate != null) {
            film.setReleaseDate(releaseDate.toLocalDate());
        }

        film.setDuration(resultSet.getInt("duration"));

        Integer ratingId = (Integer) resultSet.getObject("mpa_id");
        String mpaName = resultSet.getString("mpa_name");
        if (ratingId != null) {
            Mpa mpa = new Mpa();
            mpa.setId(ratingId);
            mpa.setName(mpaName);
            film.setMpa(mpa);
        }

        film.setGenres(new LinkedHashSet<>());
        film.setLikes(new LinkedHashSet<>());
        return film;
    }
}

