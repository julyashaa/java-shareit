package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {
    @Autowired
    JacksonTester<CommentDto> json;

    @Test
    void shouldSerializeCreated() throws Exception {
        CommentDto dto = new CommentDto();
        dto.setId(1L);
        dto.setText("Great!");
        dto.setAuthorName("Booker");
        dto.setCreated(LocalDateTime.of(2026, 1, 2, 12, 0));

        JsonContent<CommentDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-01-02T12:00:00");
    }
}