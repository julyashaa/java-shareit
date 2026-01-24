package ru.practicum.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.item.client.ItemClient;
import ru.practicum.item.dto.CommentCreateDto;
import ru.practicum.item.dto.ItemCreateDto;
import ru.practicum.item.dto.ItemUpdateDto;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader(USER_ID_HEADER) long ownerId,
                                      @Valid @RequestBody ItemCreateDto dto) {
        return itemClient.add(ownerId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(USER_ID_HEADER) long ownerId,
                                         @PathVariable long itemId,
                                         @RequestBody ItemUpdateDto dto) {
        return itemClient.update(ownerId, itemId, dto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader(USER_ID_HEADER) long ownerId,
                                          @PathVariable long itemId) {
        return itemClient.getById(ownerId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll(@RequestHeader(USER_ID_HEADER) long ownerId) {
        return itemClient.getAll(ownerId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam(required = false) String text) {
        if (text == null || text.isBlank()) {
            return ResponseEntity.ok(List.of());
        }

        return itemClient.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_ID_HEADER) long userId,
                                             @PathVariable long itemId,
                                             @Valid @RequestBody CommentCreateDto dto) {
        return itemClient.addComment(userId, itemId, dto);
    }
}
