package ru.practicum.shareit.item.storage;

import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private long nextId = 1;

    @Override
    public Item add(Item item) {
        item.setId(nextId++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item getById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NoSuchElementException("Вещь с id = " + itemId + " не найдена");
        }
        return item;
    }

    @Override
    public List<Item> getAllByOwner(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner() != null
                        && java.util.Objects.equals(item.getOwner().getId(), ownerId))
                .sorted(Comparator.comparing(Item::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> searchAvailable(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String query = text.toLowerCase();

        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> contains(item.getName(), query) || contains(item.getDescription(), query))
                .sorted(Comparator.comparing(Item::getId))
                .collect(Collectors.toList());

    }

    private boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }
}
