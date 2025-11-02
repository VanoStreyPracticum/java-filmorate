package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    Optional<User> getUser(long id);

    boolean existsUser(long id);

    Collection<User> getAllUsers();

    void addFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    Collection<User> getFriendsList(Long userId);

    List<User> getCommonFriends(Long userId, Long otherId);
}