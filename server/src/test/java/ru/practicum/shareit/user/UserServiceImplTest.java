package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceImplTest {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Test
    void createShouldSaveUser() {
        UserDto dto = new UserDto();
        dto.setName("Юля");
        dto.setEmail("yulia@mail.com");

        UserDto saved = userService.create(dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Юля");
        assertThat(saved.getEmail()).isEqualTo("yulia@mail.com");

        assertThat(userRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    void createShouldFailWhenEmailAlreadyExistsIgnoreCase() {
        UserDto first = new UserDto();
        first.setName("A");
        first.setEmail("Owner@Mail.com");
        userService.create(first);

        UserDto second = new UserDto();
        second.setName("B");
        second.setEmail("owner@mail.com");

        assertThatThrownBy(() -> userService.create(second))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Такой email уже существует");
    }

    @Test
    void updateShouldUpdateOnlyProvidedFields() {
        UserDto created = new UserDto();
        created.setName("Old");
        created.setEmail("old@mail.com");
        UserDto saved = userService.create(created);

        UserDto patch = new UserDto();
        patch.setName("New");

        UserDto updated = userService.update(saved.getId(), patch);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getEmail()).isEqualTo("old@mail.com"); // email не трогали
    }

    @Test
    void updateShouldFailWhenEmailBecomesNotUnique() {
        userService.create(user("U1", "one@mail.com"));
        UserDto saved2 = userService.create(user("U2", "two@mail.com"));

        UserDto patch = new UserDto();
        patch.setEmail("ONE@mail.com");

        assertThatThrownBy(() -> userService.update(saved2.getId(), patch))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Такой email уже существует");
    }

    @Test
    void getByIdShouldReturnUser() {
        UserDto dto = new UserDto();
        dto.setName("User");
        dto.setEmail("user@mail.com");
        UserDto saved = userService.create(dto);

        UserDto found = userService.getById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getName()).isEqualTo("User");
        assertThat(found.getEmail()).isEqualTo("user@mail.com");
    }

    @Test
    void getByIdShouldFailWhenUserNotFound() {
        assertThatThrownBy(() -> userService.getById(9999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    void getAllShouldReturnAllUsers() {
        userService.create(user("A", "a@mail.com"));
        userService.create(user("B", "b@mail.com"));

        List<UserDto> all = userService.getAll();

        assertThat(all).hasSize(2);
        assertThat(all)
                .extracting(UserDto::getEmail)
                .containsExactlyInAnyOrder("a@mail.com", "b@mail.com");
    }

    @Test
    void deleteShouldRemoveUser() {
        UserDto saved = userService.create(user("ToDelete", "del@mail.com"));

        userService.delete(saved.getId());

        assertThat(userRepository.existsById(saved.getId())).isFalse();
        assertThatThrownBy(() -> userService.getById(saved.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    void deleteShouldFailWhenUserNotFound() {
        assertThatThrownBy(() -> userService.delete(12345L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    private UserDto user(String name, String email) {
        UserDto dto = new UserDto();
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }
}
