package ru.practicum.shareit.request.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.model.Item;

@Mapper(componentModel = "spring")
public interface ItemRequestItemMapper {
    @Mapping(target = "ownerId", source = "owner.id")
    ItemRequestItemDto toDto(Item item);
}
