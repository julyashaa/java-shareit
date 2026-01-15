package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService itemRequestService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemRequestDto create(@RequestHeader(USER_ID_HEADER) long userId,
                                 @RequestBody ItemRequestCreateDto dto) {
        return itemRequestService.create(userId, dto);
    }

    // GET /requests
    @GetMapping
    public List<ItemRequestDto> getOwn(@RequestHeader(USER_ID_HEADER) long userId) {
        return itemRequestService.getOwn(userId);
    }

    // GET /requests/all
    @GetMapping("/all")
    public List<ItemRequestDto> getOthers(@RequestHeader(USER_ID_HEADER) long userId) {
        return itemRequestService.getOthers(userId);
    }

    // GET /requests/{requestId}
    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@RequestHeader(USER_ID_HEADER) long userId,
                                  @PathVariable long requestId) {
        return itemRequestService.getById(userId, requestId);
    }
}
