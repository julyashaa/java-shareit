package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        checkEmailUnique(userDto.getEmail(), null);
        User user = userMapper.toModel(userDto);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto update(Long userId, UserDto userDto) {
        User updated = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: id=" + userId));

        if (userDto.getName() != null) {
            updated.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            checkEmailUnique(userDto.getEmail(), userId);
            updated.setEmail(userDto.getEmail());
        }

        return userMapper.toDto(userRepository.save(updated));
    }

    @Override
    public UserDto getById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: id=" + userId));
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Пользователь не найден: id=" + userId);
        }
        userRepository.deleteById(userId);
    }

    private void checkEmailUnique(String email, Long userId) {
        if (email == null) return;

        boolean exists = (userId == null)
                ? userRepository.existsByEmailIgnoreCase(email)
                : userRepository.existsByEmailIgnoreCaseAndIdNot(email, userId);

        if (exists) {
            throw new RuntimeException("Такой email уже существует. Попробуйте другой.");
        }
    }
}
