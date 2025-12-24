package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item add(Item item);

    Item update(Item item);

    Item getById(Long itemId);

    List<Item> getAllByOwner(Long ownerId);

    List<Item> searchAvailable(String text);
}
