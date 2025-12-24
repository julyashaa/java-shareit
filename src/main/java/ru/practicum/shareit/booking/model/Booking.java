package ru.practicum.shareit.booking.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
public class Booking {
    private Long id;
    private Long itemId;
    private Long bookerId;

    private LocalDateTime start;
    private LocalDateTime end;

    private BookingStatus status;
}
