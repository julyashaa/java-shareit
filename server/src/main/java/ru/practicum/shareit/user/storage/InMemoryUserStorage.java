package ru.practicum.shareit.user.storage;

import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.user.model.User;

import java.util.*;


@RequiredArgsConstructor
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NoSuchElementException("Пользователь с id = " + userId + "не найден");
        }
        return user;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(Long userId) {
        if (users.get(userId) == null) {
            throw new NoSuchElementException("Пользователь с id = " + userId + "не найден");
        }

        users.remove(userId);
    }

    @Override
    public boolean exists(Long userId) {
        return users.containsKey(userId);
    }
}
