package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ItemRequestItemDto {
    Long id;
    String name;
    Long ownerId;
}
