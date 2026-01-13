package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingCreateRequestDtoJsonTest {
    @Autowired
    private JacksonTester<BookingCreateRequestDto> json;

    @Test
    void shouldSerialize() throws Exception {
        BookingCreateRequestDto dto = new BookingCreateRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.of(2026, 1, 20, 10, 0));
        dto.setEnd(LocalDateTime.of(2026, 1, 21, 10, 0));

        JsonContent<BookingCreateRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-01-20T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2026-01-21T10:00:00");
    }

    @Test
    void shouldDeserialize() throws Exception {
        String content = "{"
                + "\"itemId\": 1,"
                + "\"start\": \"2026-01-20T10:00:00\","
                + "\"end\": \"2026-01-21T10:00:00\""
                + "}";

        BookingCreateRequestDto dto = json.parseObject(content);

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 1, 20, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 1, 21, 10, 0));
    }
}
