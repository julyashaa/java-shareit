package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.dto.BookingShortMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final BookingShortMapper bookingShortMapper;
    private final CommentMapper commentMapper;


    @Override
    @Transactional
    public ItemDto add(Long ownerId, ItemDto itemDto) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id = " + ownerId + " не найден"));

        Item item = itemMapper.toModel(itemDto);
        item.setOwner(owner);

        Item saved = itemRepository.save(item);

        return itemMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id = " + ownerId + " не найден"));

        Item updated = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с id = " + itemId + " не найдена"));

        if (!updated.getOwner().getId().equals(ownerId)) {
            throw new NoSuchElementException("Редактировать вещь может только владелец");
        }

        if (itemDto.getName() != null) {
            updated.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            updated.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            updated.setAvailable(itemDto.getAvailable());
        }

        Item saved = itemRepository.save(updated);
        return itemMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentCreateDto dto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id = " + userId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с id = " + itemId + " не найдена"));

        boolean hasBooking = bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, userId, BookingStatus.APPROVED, LocalDateTime.now()
        );

        if (!hasBooking) {
            throw new IllegalArgumentException("Комментарий можно оставить только после завершённой аренды");
        }

        Comment comment = Comment.builder()
                .text(dto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    public ItemDto getById(Long ownerId, Long itemId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id = " + ownerId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь с id = " + itemId + " не найдена"));

        ItemDto dto = itemMapper.toDto(item);
        dto.setComments(
                commentRepository.findAllByItemIdOrderByCreatedDesc(itemId).stream()
                        .map(commentMapper::toDto)
                        .toList()
        );

        boolean isOwner = item.getOwner().getId().equals(ownerId);
        if (isOwner) {
            LocalDateTime now = LocalDateTime.now();

            bookingRepository.findAllByItemIdInAndStatusAndStartBeforeOrderByStartDesc(
                            List.of(itemId), BookingStatus.APPROVED, now
                    ).stream()
                    .findFirst()
                    .ifPresent(b -> dto.setLastBooking(bookingShortMapper.toDto(b)));

            bookingRepository.findAllByItemIdInAndStatusAndStartAfterOrderByStartAsc(
                            List.of(itemId), BookingStatus.APPROVED, now
                    ).stream()
                    .findFirst()
                    .ifPresent(b -> dto.setNextBooking(bookingShortMapper.toDto(b)));
        }
        return dto;
    }

    @Override
    public List<ItemDto> getAll(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id = " + ownerId + " не найден"));

        List<Item> items = itemRepository.findAllByOwnerIdOrderByIdAsc(ownerId);

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        Map<Long, BookingShortDto> lastBookings = new HashMap<>();
        Map<Long, BookingShortDto> nextBookings = new HashMap<>();
        Map<Long, List<CommentDto>> commentsByItemId = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        if (!itemIds.isEmpty()) {

            // LAST bookings (прошлые, самые поздние)
            List<Booking> last = bookingRepository
                    .findAllByItemIdInAndStatusAndStartBeforeOrderByStartDesc(
                            itemIds, BookingStatus.APPROVED, now);

            for (Booking booking : last) {
                Long itemId = booking.getItem().getId();
                lastBookings.putIfAbsent(itemId, bookingShortMapper.toDto(booking));
            }

            // NEXT bookings (будущие, самые ранние)
            List<Booking> next = bookingRepository
                    .findAllByItemIdInAndStatusAndStartAfterOrderByStartAsc(
                            itemIds, BookingStatus.APPROVED, now);

            for (Booking booking : next) {
                Long itemId = booking.getItem().getId();
                nextBookings.putIfAbsent(itemId, bookingShortMapper.toDto(booking));
            }

            commentRepository.findAllByItemIdIn(itemIds).forEach(c -> {
                Long id = c.getItem().getId();
                commentsByItemId
                        .computeIfAbsent(id, k -> new ArrayList<>())
                        .add(commentMapper.toDto(c));
            });
        }

        return items.stream()
                .map(item -> {
                    ItemDto dto = itemMapper.toDto(item);
                    dto.setLastBooking(lastBookings.get(item.getId()));
                    dto.setNextBooking(nextBookings.get(item.getId()));
                    dto.setComments(commentsByItemId.getOrDefault(item.getId(), List.of()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.searchAvailable(text).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }
}
