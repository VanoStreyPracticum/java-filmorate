package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    private final UserRowMapper userRowMapper = new UserRowMapper();

    private static final String SQL_INSERT_USER = """
        INSERT INTO users (email, login, name, birthday)
        VALUES (?, ?, ?, ?)
        """;

    private static final String SQL_UPDATE_USER = """
        UPDATE users
        SET email = ?, login = ?, name = ?, birthday = ?
        WHERE id = ?
        """;

    private static final String SQL_DELETE_USER = """
        DELETE FROM users
        WHERE id = ?
        """;

    private static final String SQL_SELECT_USER_BY_ID = """
        SELECT id, email, login, name, birthday
        FROM users
        WHERE id = ?
        """;

    private static final String SQL_SELECT_ALL_USERS = """
        SELECT id, email, login, name, birthday
        FROM users
        """;

    private static final String SQL_EXISTS_USER = """
        SELECT COUNT(*) > 0
        FROM users
        WHERE id = ?
        """;

    private static final String SQL_INSERT_FRIEND = """
        MERGE INTO friends (user_id, friend_id, status)
        KEY (user_id, friend_id)
        VALUES (?, ?, ?)
        """;

    private static final String SQL_DELETE_FRIEND = """
        DELETE FROM friends
        WHERE user_id = ? AND friend_id = ?
        """;

    private static final String SQL_SELECT_USER_FRIENDS = """
        SELECT u.id, u.email, u.login, u.name, u.birthday
        FROM friends f
        JOIN users u ON f.friend_id = u.id
        WHERE f.user_id = ?
        ORDER BY u.id
        """;

    private static final String SQL_SELECT_COMMON_FRIENDS = """
        SELECT u.id, u.email, u.login, u.name, u.birthday
        FROM friends f1
        JOIN friends f2 ON f1.friend_id = f2.friend_id
        JOIN users u ON f1.friend_id = u.id
        WHERE f1.user_id = ? AND f2.user_id = ?
        ORDER BY u.id
        """;

    private static final String SQL_SELECT_FRIENDSHIP_STATUS = """
        SELECT status
        FROM friends
        WHERE user_id = ? AND friend_id = ?
        """;

    private static final String SQL_CONFIRM_FRIENDSHIP = """
        UPDATE friends
        SET status = TRUE
        WHERE user_id = ? AND friend_id = ?
        """;

    private static final String SQL_DOWNGRADE_FRIENDSHIP = """
        UPDATE friends
        SET status = FALSE
        WHERE user_id = ? AND friend_id = ? AND status = TRUE
        """;

    @Override
    public User addUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(SQL_INSERT_USER, new String[]{"id"});
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getLogin());
            statement.setString(3, user.getName());
            statement.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return statement;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User updateUser(User user) {
        requireUserExists(user.getId());

        jdbcTemplate.update(SQL_UPDATE_USER,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                java.sql.Date.valueOf(user.getBirthday()),
                user.getId());

        return user;
    }

    @Override
    public boolean deleteUser(long userId) {
        requireUserExists(userId);
        return jdbcTemplate.update(SQL_DELETE_USER, userId) > 0;
    }

    @Override
    public Optional<User> getUser(long userId) {
        return jdbcTemplate.query(SQL_SELECT_USER_BY_ID, userRowMapper, userId)
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsUser(long userId) {
        return jdbcTemplate.queryForObject(SQL_EXISTS_USER, Boolean.class, userId);
    }

    @Override
    public Collection<User> getAllUsers() {
        return jdbcTemplate.query(SQL_SELECT_ALL_USERS, userRowMapper);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        requireUserExists(userId);
        requireUserExists(friendId);

        List<Boolean> reverseStatuses = jdbcTemplate.queryForList(
                SQL_SELECT_FRIENDSHIP_STATUS, Boolean.class, friendId, userId
        );

        if (reverseStatuses.isEmpty()) {
            jdbcTemplate.update(SQL_INSERT_FRIEND, userId, friendId, false);
        } else {
            jdbcTemplate.update(SQL_CONFIRM_FRIENDSHIP, userId, friendId);
            jdbcTemplate.update(SQL_INSERT_FRIEND, friendId, userId, true);
        }
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        jdbcTemplate.update(SQL_DELETE_FRIEND, userId, friendId);
        jdbcTemplate.update(SQL_DOWNGRADE_FRIENDSHIP, friendId, userId);
    }

    @Override
    public Collection<User> getFriendsList(Long userId) {
        List<User> friends = jdbcTemplate.query(SQL_SELECT_USER_FRIENDS, userRowMapper, userId);
        friends.forEach(friend -> friend.setFriends(Collections.singleton(friend.getId())));
        return friends;
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        List<User> commonFriends = jdbcTemplate.query(SQL_SELECT_COMMON_FRIENDS, userRowMapper, userId, otherUserId);
        commonFriends.forEach(friend -> friend.setFriends(Collections.singleton(friend.getId())));
        return commonFriends;
    }

    private void requireUserExists(Long userId) {
        if (!existsUser(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }
}
