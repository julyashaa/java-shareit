package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ItemRequestServiceImplTest {
    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private ItemRepository itemRepository;

    @Test
    void createShouldSaveRequest_andReturnDtoWithEmptyItems() {
        User requestor = userRepository.save(User.builder()
                .name("Req")
                .email("req@mail.com")
                .build());

        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("Need a drill");

        ItemRequestDto created = requestService.create(requestor.getId(), dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Need a drill");
        assertThat(created.getCreated()).isNotNull();
        assertThat(created.getItems()).isEmpty();

        ItemRequest fromDb = itemRequestRepository.findById(created.getId()).orElseThrow();
        assertThat(fromDb.getRequestor().getId()).isEqualTo(requestor.getId());
        assertThat(fromDb.getDescription()).isEqualTo("Need a drill");
    }

    @Test
    void createShouldFailWhenUserNotFound() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("Need");

        assertThatThrownBy(() -> requestService.create(9999L, dto))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Пользователь с id = 9999 не найден");
    }

    @Test
    void getOwnShouldReturnOwnRequestsWithItems_sortedDescByCreated() {
        User requestor = saveUser("Req", "req@mail.com");
        User owner = saveUser("Owner", "owner@mail.com");

        ItemRequest r1 = itemRequestRepository.save(ItemRequest.builder()
                .description("Need drill")
                .requestor(requestor)
                .created(java.time.LocalDateTime.now().minusMinutes(10))
                .build());

        ItemRequest r2 = itemRequestRepository.save(ItemRequest.builder()
                .description("Need ladder")
                .requestor(requestor)
                .created(java.time.LocalDateTime.now().minusMinutes(5))
                .build());

        itemRepository.save(Item.builder()
                .name("Drill")
                .description("Powerful")
                .available(true)
                .owner(owner)
                .request(r1)
                .build());

        itemRepository.save(Item.builder()
                .name("Ladder")
                .description("Big")
                .available(true)
                .owner(owner)
                .request(r2)
                .build());

        List<ItemRequestDto> result = requestService.getOwn(requestor.getId());

        assertThat(result).hasSize(2);

        assertThat(result.get(0).getId()).isEqualTo(r2.getId());
        assertThat(result.get(1).getId()).isEqualTo(r1.getId());

        assertThat(result.get(0).getItems()).hasSize(1);
        assertThat(result.get(0).getItems().get(0).getName()).isEqualTo("Ladder");

        assertThat(result.get(1).getItems()).hasSize(1);
        assertThat(result.get(1).getItems().get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void getOwnShouldReturnEmptyListWhenNoRequests() {
        User requestor = saveUser("Req", "req@mail.com");

        List<ItemRequestDto> result = requestService.getOwn(requestor.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void getOthersShouldReturnOnlyNotOwnRequestsWithItems() {
        User user = saveUser("User", "user@mail.com");
        User other = saveUser("Other", "other@mail.com");
        User owner = saveUser("Owner", "owner@mail.com");

        ItemRequest otherReq = itemRequestRepository.save(ItemRequest.builder()
                .description("Other needs drill")
                .requestor(other)
                .created(java.time.LocalDateTime.now().minusMinutes(1))
                .build());

        itemRequestRepository.save(ItemRequest.builder()
                .description("User needs ladder")
                .requestor(user)
                .created(java.time.LocalDateTime.now().minusMinutes(2))
                .build());

        itemRepository.save(Item.builder()
                .name("Drill")
                .description("Powerful")
                .available(true)
                .owner(owner)
                .request(otherReq)
                .build());

        List<ItemRequestDto> result = requestService.getOthers(user.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(otherReq.getId());
        assertThat(result.get(0).getItems()).hasSize(1);
        assertThat(result.get(0).getItems().get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void getByIdShouldReturnRequestWithItems() {
        User requestor = saveUser("Req", "req@mail.com");
        User owner = saveUser("Owner", "owner@mail.com");

        ItemRequest request = itemRequestRepository.save(ItemRequest.builder()
                .description("Need drill")
                .requestor(requestor)
                .created(java.time.LocalDateTime.now().minusMinutes(1))
                .build());

        itemRepository.save(Item.builder()
                .name("Drill")
                .description("Powerful")
                .available(true)
                .owner(owner)
                .request(request)
                .build());

        ItemRequestDto dto = requestService.getById(requestor.getId(), request.getId());

        assertThat(dto.getId()).isEqualTo(request.getId());
        assertThat(dto.getDescription()).isEqualTo("Need drill");
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Drill");
    }

    @Test
    void getByIdShouldFailWhenRequestNotFound() {
        User user = saveUser("User", "user@mail.com");

        assertThatThrownBy(() -> requestService.getById(user.getId(), 9999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Запрос с id = 9999 не найден");
    }

    @Test
    void getOwnShouldFailWhenUserNotFound() {
        assertThatThrownBy(() -> requestService.getOwn(9999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Пользователь с id = 9999 не найден");
    }

    @Test
    void getOthersShouldFailWhenUserNotFound() {
        assertThatThrownBy(() -> requestService.getOthers(9999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Пользователь с id = 9999 не найден");
    }

    @Test
    void getByIdShouldFailWhenUserNotFound() {
        assertThatThrownBy(() -> requestService.getById(9999L, 1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Пользователь с id = 9999 не найден");
    }

    private User saveUser(String name, String email) {
        return userRepository.save(User.builder().name(name).email(email).build());
    }
}
