package ru.practicum.shareit.request.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@Data
public class ItemRequestDto {
    private Long id;
    private String description;
    // private Long requestorId;
    private LocalDateTime created;
    private List<ItemRequestItemDto> items;
}
