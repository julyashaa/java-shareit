package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
public class BookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;

    private ItemInfo item;
    private BookerInfo booker;

    @Data
    public static class ItemInfo {
        private Long id;
        private String name;
    }

    @Data
    public static class BookerInfo {
        private Long id;
    }
}
