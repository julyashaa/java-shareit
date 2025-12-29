package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto add(Long ownerId, ItemDto itemDto) {
        if (!userStorage.exists(ownerId)) {
            throw new NoSuchElementException("Пользователь с id = " + ownerId + " не найден");
        }
        userStorage.getById(ownerId);
        Item item = itemMapper.toModel(itemDto);
        item.setOwnerId(ownerId);
        Item saved = itemStorage.add(item);

        return itemMapper.toDto(saved);
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        userStorage.getById(ownerId);
        Item updated = itemStorage.getById(itemId);

        if (!updated.getOwnerId().equals(ownerId)) {
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

        itemStorage.update(updated);

        return itemMapper.toDto(updated);
    }

    @Override
    public ItemDto getById(Long ownerId, Long itemId) {
        userStorage.getById(ownerId);
        return itemMapper.toDto(itemStorage.getById(itemId));
    }

    @Override
    public List<ItemDto> getAll(Long ownerId) {
        userStorage.getById(ownerId);
        return itemStorage.getAllByOwner(ownerId).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemStorage.searchAvailable(text).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }
}
