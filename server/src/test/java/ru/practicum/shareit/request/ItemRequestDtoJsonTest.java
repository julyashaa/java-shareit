package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void shouldSerialize() throws Exception {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(10L);
        dto.setDescription("Need a drill");
        dto.setCreated(LocalDateTime.of(2026, 1, 1, 10, 0));
        dto.setItems(List.of()); // важно: items присутствует

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Need a drill");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-01-01T10:00:00");
        assertThat(result).extractingJsonPathArrayValue("$.items").isEmpty();
    }

    @Test
    void shouldDeserialize() throws Exception {
        String content = "{"
                + "\"id\": 10,"
                + "\"description\": \"Need a drill\","
                + "\"created\": \"2026-01-01T10:00:00\","
                + "\"items\": []"
                + "}";

        ItemRequestDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getDescription()).isEqualTo("Need a drill");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2026, 1, 1, 10, 0));
        assertThat(dto.getItems()).isEmpty();
    }
}
