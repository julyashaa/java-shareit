package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ItemServiceImplIT {
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void getAllShouldReturnItemsWithNextBookingAndComments() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@mail.com")
                .build());

        User booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@mail.com")
                .build());

        Item item = itemRepository.save(Item.builder()
                .name("Drill")
                .description("Powerful")
                .available(true)
                .owner(owner)
                .build());


        LocalDateTime pastStart = LocalDateTime.now().minusDays(2);
        LocalDateTime pastEnd = LocalDateTime.now().minusDays(1);

        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(pastStart)
                .end(pastEnd)
                .status(BookingStatus.APPROVED)
                .build());

        CommentCreateDto comment = new CommentCreateDto();
        comment.setText("Great!");
        itemService.addComment(booker.getId(), item.getId(), comment);

        LocalDateTime futureStart = LocalDateTime.now().plusDays(1);
        LocalDateTime futureEnd = futureStart.plusDays(1);

        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(futureStart)
                .end(futureEnd)
                .status(BookingStatus.APPROVED)
                .build());

        List<ItemDto> result = itemService.getAll(owner.getId());

        assertThat(result).hasSize(1);

        ItemDto dto = result.get(0);
        assertThat(dto.getId()).isEqualTo(item.getId());
        assertThat(dto.getName()).isEqualTo("Drill");

        assertThat(dto.getComments()).hasSize(1);
        assertThat(dto.getComments().get(0).getText()).isEqualTo("Great!");

        assertThat(dto.getNextBooking()).isNotNull();
        assertThat(dto.getNextBooking().getId()).isNotNull();
    }

    @Test
    void createShouldSaveItem() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@mail.com")
                .build());

        ItemDto createDto = new ItemDto();
        createDto.setName("Drill");
        createDto.setDescription("Powerful");
        createDto.setAvailable(true);

        ItemDto saved = itemService.add(owner.getId(), createDto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Drill");
        assertThat(saved.getDescription()).isEqualTo("Powerful");
        assertThat(saved.getAvailable()).isTrue();

        Item fromDb = itemRepository.findById(saved.getId()).orElseThrow();
        assertThat(fromDb.getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    void updateShouldUpdateItemForOwner() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@mail.com")
                .build());

        Item item = itemRepository.save(Item.builder()
                .name("Drill")
                .description("Powerful")
                .available(true)
                .owner(owner)
                .build());

        ItemDto patch = new ItemDto();
        patch.setName("Super drill");

        ItemDto updated = itemService.update(owner.getId(), item.getId(), patch);

        assertThat(updated.getId()).isEqualTo(item.getId());
        assertThat(updated.getName()).isEqualTo("Super drill");
        assertThat(updated.getDescription()).isEqualTo("Powerful"); // не трогали
    }

    @Test
    void updateShouldFailForNotOwner() {
        User owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@mail.com")
                .build());
        User other = userRepository.save(User.builder()
                .name("Other")
                .email("other@mail.com")
                .build());

        Item item = itemRepository.save(Item.builder()
                .name("Drill")
                .description("Powerful")
                .available(true)
                .owner(owner)
                .build());

        ItemDto patch = new ItemDto();
        patch.setName("Hacked");

        assertThatThrownBy(() -> itemService.update(other.getId(), item.getId(), patch))
                .isInstanceOf(RuntimeException.class); // <- замени на свое исключение
    }

    @Test
    void getByIdOwnerShouldReturnItemWithBookings() {
        LocalDateTime now = LocalDateTime.now();

        User owner = userRepository.save(User.builder().name("Owner").email("owner@mail.com").build());
        User booker = userRepository.save(User.builder().name("Booker").email("booker@mail.com").build());

        Item item = itemRepository.save(Item.builder()
                .name("Drill")
                .description("Powerful")
                .available(true)
                .owner(owner)
                .build());

        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(now.minusDays(2))
                .end(now.minusDays(1))
                .status(BookingStatus.APPROVED)
                .build());

        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .status(BookingStatus.APPROVED)
                .build());

        ItemDto dto = itemService.getById(owner.getId(), item.getId());

        assertThat(dto.getId()).isEqualTo(item.getId());
        assertThat(dto.getLastBooking()).isNotNull();
        assertThat(dto.getNextBooking()).isNotNull();
    }

    @Test
    void getByIdNotOwnerShouldReturnItemWithoutBookings() {
        LocalDateTime now = LocalDateTime.now();

        User owner = userRepository.save(User.builder().name("Owner").email("owner@mail.com").build());
        User other = userRepository.save(User.builder().name("Other").email("other@mail.com").build());
        User booker = userRepository.save(User.builder().name("Booker").email("booker@mail.com").build());

        Item item = itemRepository.save(Item.builder()
                .name("Drill")
                .description("Powerful")
                .available(true)
                .owner(owner)
                .build());

        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .status(BookingStatus.APPROVED)
                .build());

        ItemDto dto = itemService.getById(other.getId(), item.getId());

        assertThat(dto.getId()).isEqualTo(item.getId());
        assertThat(dto.getLastBooking()).isNull();
        assertThat(dto.getNextBooking()).isNull();
    }

    @Test
    void searchShouldReturnOnlyAvailableAndMatchTextIgnoreBlankText() {
        User owner = userRepository.save(User.builder().name("Owner").email("owner@mail.com").build());

        itemRepository.save(Item.builder()
                .name("Drill")
                .description("Powerful tool")
                .available(true)
                .owner(owner)
                .build());

        itemRepository.save(Item.builder()
                .name("Drill PRO")
                .description("Not available")
                .available(false)
                .owner(owner)
                .build());

        List<ItemDto> found = itemService.search("drill");
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).containsIgnoringCase("drill");

        assertThat(itemService.search("   ")).isEmpty();
        assertThat(itemService.search(null)).isEmpty();
    }

    @Test
    void addCommentShouldFailWithoutCompletedApprovedBooking() {
        User owner = userRepository.save(User.builder().name("Owner").email("owner@mail.com").build());
        User booker = userRepository.save(User.builder().name("Booker").email("booker@mail.com").build());

        Item item = itemRepository.save(Item.builder()
                .name("Drill")
                .description("Powerful")
                .available(true)
                .owner(owner)
                .build());

        // бронирование в будущем -> не завершено, коммент нельзя
        LocalDateTime now = LocalDateTime.now();
        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .status(BookingStatus.APPROVED)
                .build());

        CommentCreateDto comment = new CommentCreateDto();
        comment.setText("Great!");

        assertThatThrownBy(() -> itemService.addComment(booker.getId(), item.getId(), comment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Комментарий можно оставить только после завершённой аренды");
    }
}
