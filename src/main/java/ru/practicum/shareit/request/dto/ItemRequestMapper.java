package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.request.model.ItemRequest;

public class ItemRequestMapper {
    public static ItemRequestDto toDto(ItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(itemRequest.getId());
        dto.setDescription(itemRequest.getDescription());
        dto.setRequestorId(itemRequest.getRequestorId());
        dto.setCreated(itemRequest.getCreated());
        return dto;
    }

    public static ItemRequest toModel(ItemRequestDto itemRequestDto) {
        if (itemRequestDto == null) {
            return null;
        }

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(itemRequestDto.getId());
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setRequestorId(itemRequestDto.getRequestorId());
        itemRequest.setCreated(itemRequestDto.getCreated());
        return itemRequest;
    }
}
