package ru.practicum.shareit.item.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(target = "requestId", source = "request.id")
    ItemDto toDto(Item item);

    @Mapping(target = "request", ignore = true)
    Item toModel(ItemDto itemDto);
}
