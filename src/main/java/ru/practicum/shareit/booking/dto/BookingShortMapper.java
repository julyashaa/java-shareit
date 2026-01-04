package ru.practicum.shareit.booking.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.model.Booking;

@Mapper(componentModel = "spring")
public interface BookingShortMapper {

    @Mapping(target = "bookerId", source = "booker.id")
    BookingShortDto toDto(Booking booking);
}
