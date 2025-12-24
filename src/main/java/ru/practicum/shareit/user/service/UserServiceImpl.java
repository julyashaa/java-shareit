package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public UserDto create(UserDto userDto) {
        checkEmailUnique(userDto.getEmail(), null);
        User user = UserMapper.toModel(userDto);
        return UserMapper.toDto(userStorage.create(user));
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User updated = userStorage.getById(userId);
        if (userDto.getName() != null) {
            updated.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            checkEmailUnique(userDto.getEmail(), userId);
            updated.setEmail(userDto.getEmail());
        }

        return UserMapper.toDto(userStorage.update(updated));
    }

    @Override
    public UserDto getById(Long userId) {
        return UserMapper.toDto(userStorage.getById(userId));
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage.getAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long userId) {
        userStorage.delete(userId);
    }

    private void checkEmailUnique(String email, Long userId) {
        if (email == null) return;


        for (User user : userStorage.getAll()) {
            if (email.equalsIgnoreCase(user.getEmail())
                && !user.getId().equals(userId)) {
                throw new RuntimeException("Такой email уже существует. Попробуйте другой.");
            }
        }
    }
}
