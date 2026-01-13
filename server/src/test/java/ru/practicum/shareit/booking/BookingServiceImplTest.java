package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BookingServiceImplTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    @Test
    void createShouldSaveBookingWithWaitingStatus() {
        LocalDateTime now = LocalDateTime.now();

        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item item = saveItem(owner, "Drill", true);

        BookingCreateRequestDto dto = new BookingCreateRequestDto();
        dto.setItemId(item.getId());
        dto.setStart(now.plusDays(1));
        dto.setEnd(now.plusDays(2));

        BookingDto saved = bookingService.create(booker.getId(), dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(BookingStatus.WAITING);

        Booking fromDb = bookingRepository.findById(saved.getId()).orElseThrow();
        assertThat(fromDb.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(fromDb.getItem().getId()).isEqualTo(item.getId());
        assertThat(fromDb.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void createShouldFailWhenOwnerBooksOwnItem() {
        LocalDateTime now = LocalDateTime.now();

        User owner = saveUser("Owner", "owner@mail.com");
        Item item = saveItem(owner, "Drill", true);

        BookingCreateRequestDto dto = new BookingCreateRequestDto();
        dto.setItemId(item.getId());
        dto.setStart(now.plusDays(1));
        dto.setEnd(now.plusDays(2));

        assertThatThrownBy(() -> bookingService.create(owner.getId(), dto))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Нельзя бронировать свою вещь");
    }

    @Test
    void createShouldFailWhenItemNotAvailable() {
        LocalDateTime now = LocalDateTime.now();

        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item item = saveItem(owner, "Drill", false);

        BookingCreateRequestDto dto = new BookingCreateRequestDto();
        dto.setItemId(item.getId());
        dto.setStart(now.plusDays(1));
        dto.setEnd(now.plusDays(2));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Вещь недоступна");
    }

    @Test
    void createShouldFailWhenDatesInvalidStartNotBeforeEnd() {
        LocalDateTime now = LocalDateTime.now();

        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item item = saveItem(owner, "Drill", true);

        BookingCreateRequestDto dto = new BookingCreateRequestDto();
        dto.setItemId(item.getId());
        dto.setStart(now.plusDays(2));
        dto.setEnd(now.plusDays(2)); // start == end

        assertThatThrownBy(() -> bookingService.create(booker.getId(), dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Дата начала должна быть раньше");
    }

    @Test
    void createShouldFailWhenStartInPast() {
        LocalDateTime now = LocalDateTime.now();

        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item item = saveItem(owner, "Drill", true);

        BookingCreateRequestDto dto = new BookingCreateRequestDto();
        dto.setItemId(item.getId());
        dto.setStart(now.minusHours(1));
        dto.setEnd(now.plusDays(1));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Дата начала не может быть в прошлом");
    }

    @Test
    void approveShouldSetApprovedWhenOwnerApproves() {
        LocalDateTime now = LocalDateTime.now();

        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item item = saveItem(owner, "Drill", true);

        Booking booking = saveBooking(item, booker,
                now.plusDays(1), now.plusDays(2), BookingStatus.WAITING);

        BookingDto approved = bookingService.approve(owner.getId(), booking.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(bookingRepository.findById(booking.getId()).orElseThrow().getStatus())
                .isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approveShouldFailWhenNotOwner() {
        LocalDateTime now = LocalDateTime.now();

        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        User other = saveUser("Other", "other@mail.com");
        Item item = saveItem(owner, "Drill", true);

        Booking booking = saveBooking(item, booker,
                now.plusDays(1), now.plusDays(2), BookingStatus.WAITING);

        assertThatThrownBy(() -> bookingService.approve(other.getId(), booking.getId(), true))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("только владелец вещи");
    }

    @Test
    void approveShouldFailWhenDecisionAlreadyMade() {
        LocalDateTime now = LocalDateTime.now();

        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item item = saveItem(owner, "Drill", true);

        Booking booking = saveBooking(item, booker,
                now.plusDays(1), now.plusDays(2), BookingStatus.WAITING);

        bookingService.approve(owner.getId(), booking.getId(), true);

        assertThatThrownBy(() -> bookingService.approve(owner.getId(), booking.getId(), true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Решение уже принято");
    }

    @Test
    void cancelShouldSetCanceledWhenBookerCancelsFutureBooking() {
        LocalDateTime now = LocalDateTime.now();

        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item item = saveItem(owner, "Drill", true);

        Booking booking = saveBooking(item, booker,
                now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED);

        BookingDto canceled = bookingService.cancel(booker.getId(), booking.getId());

        assertThat(canceled.getStatus()).isEqualTo(BookingStatus.CANCELED);
        assertThat(bookingRepository.findById(booking.getId()).orElseThrow().getStatus())
                .isEqualTo(BookingStatus.CANCELED);
    }

    @Test
    void cancelShouldFailWhenNotBooker() {
        LocalDateTime now = LocalDateTime.now();

        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        User other = saveUser("Other", "other@mail.com");
        Item item = saveItem(owner, "Drill", true);

        Booking booking = saveBooking(item, booker,
                now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED);

        assertThatThrownBy(() -> bookingService.cancel(other.getId(), booking.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("только автор");
    }

    @Test
    void cancelShouldReturnSameWhenAlreadyCanceled() {
        LocalDateTime now = LocalDateTime.now();

        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item item = saveItem(owner, "Drill", true);

        Booking booking = saveBooking(item, booker,
                now.plusDays(1), now.plusDays(2), BookingStatus.CANCELED);

        BookingDto canceled = bookingService.cancel(booker.getId(), booking.getId());
        assertThat(canceled.getStatus()).isEqualTo(BookingStatus.CANCELED);
    }

    @Test
    void cancelShouldFailWhenBookingFinished() {
        LocalDateTime now = LocalDateTime.now();

        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item item = saveItem(owner, "Drill", true);

        Booking booking = saveBooking(item, booker,
                now.minusDays(2), now.minusDays(1), BookingStatus.APPROVED);

        assertThatThrownBy(() -> bookingService.cancel(booker.getId(), booking.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Нельзя отменить завершённое");
    }

    @Test
    void cancelShouldFailWhenBookingRejected() {
        LocalDateTime now = LocalDateTime.now();

        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item item = saveItem(owner, "Drill", true);

        Booking booking = saveBooking(item, booker,
                now.plusDays(1), now.plusDays(2), BookingStatus.REJECTED);

        assertThatThrownBy(() -> bookingService.cancel(booker.getId(), booking.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Нельзя отменить отклонённое");
    }

    @Test
    void getByIdShouldAllowOwnerAndBooker() {
        LocalDateTime now = LocalDateTime.now();

        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        Item item = saveItem(owner, "Drill", true);

        Booking booking = saveBooking(item, booker,
                now.plusDays(1), now.plusDays(2), BookingStatus.WAITING);

        BookingDto byBooker = bookingService.getById(booker.getId(), booking.getId());
        BookingDto byOwner = bookingService.getById(owner.getId(), booking.getId());

        assertThat(byBooker.getId()).isEqualTo(booking.getId());
        assertThat(byOwner.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getByIdShouldFailForThirdUser() {
        LocalDateTime now = LocalDateTime.now();

        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");
        User other = saveUser("Other", "other@mail.com");
        Item item = saveItem(owner, "Drill", true);

        Booking booking = saveBooking(item, booker,
                now.plusDays(1), now.plusDays(2), BookingStatus.WAITING);

        assertThatThrownBy(() -> bookingService.getById(other.getId(), booking.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Нет доступа");
    }

    @Test
    void getByBookerShouldReturnBookingsByState() {
        LocalDateTime now = LocalDateTime.now();

        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");

        Item item1 = saveItem(owner, "Item1", true);
        Item item2 = saveItem(owner, "Item2", true);
        Item item3 = saveItem(owner, "Item3", true);

        // PAST
        saveBooking(item1, booker, now.minusDays(3), now.minusDays(2), BookingStatus.APPROVED);
        // CURRENT
        saveBooking(item2, booker, now.minusHours(1), now.plusHours(1), BookingStatus.APPROVED);
        // FUTURE
        saveBooking(item3, booker, now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED);
        // WAITING & REJECTED (по статусу)
        Booking waiting = saveBooking(item3, booker, now.plusDays(3), now.plusDays(4), BookingStatus.WAITING);
        Booking rejected = saveBooking(item3, booker, now.plusDays(5), now.plusDays(6), BookingStatus.REJECTED);

        assertThat(bookingService.getByBooker(booker.getId(), "ALL")).hasSize(5);

        assertThat(bookingService.getByBooker(booker.getId(), "PAST"))
                .extracting(BookingDto::getStatus)
                .containsOnly(BookingStatus.APPROVED);

        assertThat(bookingService.getByBooker(booker.getId(), "CURRENT")).hasSize(1);
        assertThat(bookingService.getByBooker(booker.getId(), "FUTURE")).hasSize(3); // future + waiting + rejected по времени startAfter

        assertThat(bookingService.getByBooker(booker.getId(), "WAITING"))
                .extracting(BookingDto::getId)
                .contains(waiting.getId());

        assertThat(bookingService.getByBooker(booker.getId(), "REJECTED"))
                .extracting(BookingDto::getId)
                .contains(rejected.getId());
    }

    @Test
    void getByOwnerShouldReturnBookingsByState() {
        LocalDateTime now = LocalDateTime.now();

        User owner = saveUser("Owner", "owner@mail.com");
        User booker = saveUser("Booker", "booker@mail.com");

        Item item1 = saveItem(owner, "Item1", true);
        Item item2 = saveItem(owner, "Item2", true);
        Item item3 = saveItem(owner, "Item3", true);

        saveBooking(item1, booker, now.minusDays(3), now.minusDays(2), BookingStatus.APPROVED); // past
        saveBooking(item2, booker, now.minusHours(1), now.plusHours(1), BookingStatus.APPROVED); // current
        saveBooking(item3, booker, now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED); // future
        Booking waiting = saveBooking(item3, booker, now.plusDays(3), now.plusDays(4), BookingStatus.WAITING);
        Booking rejected = saveBooking(item3, booker, now.plusDays(5), now.plusDays(6), BookingStatus.REJECTED);

        assertThat(bookingService.getByOwner(owner.getId(), "ALL")).hasSize(5);
        assertThat(bookingService.getByOwner(owner.getId(), "CURRENT")).hasSize(1);
        assertThat(bookingService.getByOwner(owner.getId(), "PAST")).hasSize(1);

        assertThat(bookingService.getByOwner(owner.getId(), "WAITING"))
                .extracting(BookingDto::getId)
                .contains(waiting.getId());

        assertThat(bookingService.getByOwner(owner.getId(), "REJECTED"))
                .extracting(BookingDto::getId)
                .contains(rejected.getId());
    }

    private User saveUser(String name, String email) {
        return userRepository.save(User.builder().name(name).email(email).build());
    }

    private Item saveItem(User owner, String name, boolean available) {
        return itemRepository.save(Item.builder()
                .name(name)
                .description("desc")
                .available(available)
                .owner(owner)
                .build());
    }

    private Booking saveBooking(Item item, User booker, LocalDateTime start, LocalDateTime end, BookingStatus status) {
        return bookingRepository.save(Booking.builder()
                .item(item)
                .booker(booker)
                .start(start)
                .end(end)
                .status(status)
                .build());
    }
}
