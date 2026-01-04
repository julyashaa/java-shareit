package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    private static final Sort SORT_NEW_TO_OLD = Sort.by(Sort.Direction.DESC, "start");

    @Override
    @Transactional
    public BookingDto create(Long userId, BookingCreateRequestDto dto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id = " + userId + " не найден"));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NoSuchElementException("Вещь с id = " + dto.getItemId() + " не найдена"));

        if (item.getOwner().getId().equals(userId)) {
            throw new NoSuchElementException("Нельзя бронировать свою вещь");
        }

        if (Boolean.FALSE.equals(item.getAvailable())) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }

        validateDates(dto.getStart(), dto.getEnd());

        Booking booking = Booking.builder()
                .item(item)
                .booker(booker)
                .start(dto.getStart())
                .end(dto.getEnd())
                .status(BookingStatus.WAITING)
                .build();

        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Бронирование с id = " + bookingId + " не найдено"));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException("Подтвердить/отклонить может только владелец вещи");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException("Решение уже принято");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto cancel(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Бронирование с id = " + bookingId + " не найдено"));

        if (!booking.getBooker().getId().equals(userId)) {
            throw new AccessDeniedException("Отменить бронирование может только автор");
        }

        if (booking.getStatus() == BookingStatus.CANCELED) {
            return bookingMapper.toDto(booking);
        }

        if (booking.getEnd().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Нельзя отменить завершённое бронирование");
        }

        if (booking.getStatus() == BookingStatus.REJECTED) {
            throw new IllegalArgumentException("Нельзя отменить отклонённое бронирование");
        }

        booking.setStatus(BookingStatus.CANCELED);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }


    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id = " + userId + " не найден"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Бронирование с id = " + bookingId + " не найдено"));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new AccessDeniedException("Нет доступа к бронированию");
        }

        return bookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getByBooker(Long userId, String state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id = " + userId + " не найден"));

        BookingState st = BookingState.from(state);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (st) {
            case ALL -> bookingRepository.findAllByBookerId(userId, SORT_NEW_TO_OLD);
            case CURRENT ->
                    bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(userId, now, now, SORT_NEW_TO_OLD);
            case PAST -> bookingRepository.findAllByBookerIdAndEndBefore(userId, now, SORT_NEW_TO_OLD);
            case FUTURE -> bookingRepository.findAllByBookerIdAndStartAfter(userId, now, SORT_NEW_TO_OLD);
            case WAITING ->
                    bookingRepository.findAllByBookerIdAndStatus(userId, BookingStatus.WAITING, SORT_NEW_TO_OLD);
            case REJECTED ->
                    bookingRepository.findAllByBookerIdAndStatus(userId, BookingStatus.REJECTED, SORT_NEW_TO_OLD);
        };

        return bookings.stream().map(bookingMapper::toDto).toList();
    }

    @Override
    public List<BookingDto> getByOwner(Long ownerId, String state) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id = " + ownerId + " не найден"));

        BookingState st = BookingState.from(state);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (st) {
            case ALL -> bookingRepository.findAllByItemOwnerId(ownerId, SORT_NEW_TO_OLD);
            case CURRENT ->
                    bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfter(ownerId, now, now, SORT_NEW_TO_OLD);
            case PAST -> bookingRepository.findAllByItemOwnerIdAndEndBefore(ownerId, now, SORT_NEW_TO_OLD);
            case FUTURE -> bookingRepository.findAllByItemOwnerIdAndStartAfter(ownerId, now, SORT_NEW_TO_OLD);
            case WAITING ->
                    bookingRepository.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING, SORT_NEW_TO_OLD);
            case REJECTED ->
                    bookingRepository.findAllByItemOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, SORT_NEW_TO_OLD);
        };

        return bookings.stream().map(bookingMapper::toDto).toList();
    }


    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Дата начала и окончания обязательны");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Дата начала должна быть раньше даты окончания");
        }

        if (start.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Дата начала не может быть в прошлом");
        }
    }
}
