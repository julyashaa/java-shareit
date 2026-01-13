package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.*;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private final ItemRequestMapper requestMapper;
    private final ItemRequestItemMapper itemMapper;

    @Override
    @Transactional
    public ItemRequestDto create(long userId, ItemRequestCreateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id = " + userId + " не найден"));

        ItemRequest saved = itemRequestRepository.save(ItemRequest.builder()
                .description(dto.getDescription())
                .requestor(user)
                .created(LocalDateTime.now())
                .build());

        ItemRequestDto result = requestMapper.toDto(saved);
        result.setItems(List.of());
        return result;
    }

    @Override
    public List<ItemRequestDto> getOwn(long userId) {
        ensureUserExists(userId);

        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);

        return mapRequestsWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> getOthers(long userId) {
        ensureUserExists(userId);

        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId);

        return mapRequestsWithItems(requests);
    }

    @Override
    public ItemRequestDto getById(long userId, long requestId) {
        ensureUserExists(userId);

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Запрос с id = " + requestId + " не найден"));

        List<ItemRequestItemDto> items = itemRepository.findAllByRequest_Id(requestId).stream()
                .map(itemMapper::toDto)
                .toList();

        ItemRequestDto dto = requestMapper.toDto(request);
        dto.setItems(items);

        return dto;
    }


    private List<ItemRequestDto> mapRequestsWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> ids = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<ItemRequestItemDto>> itemsByRequestId =
                itemRepository.findAllByRequest_IdIn(ids).stream()
                        .collect(Collectors.groupingBy(
                                item -> item.getRequest().getId(),
                                Collectors.mapping(itemMapper::toDto, Collectors.toList())
                        ));

        return requests.stream()
                .map(request -> {
                    ItemRequestDto dto = requestMapper.toDto(request);
                    dto.setItems(itemsByRequestId.getOrDefault(request.getId(), List.of()));
                    return dto;
                })
                .toList();
    }

    private void ensureUserExists(long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь с id = " + userId + " не найден"));
    }
}
