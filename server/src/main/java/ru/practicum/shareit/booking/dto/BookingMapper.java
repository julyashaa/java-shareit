package ru.practicum.shareit.booking.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.model.Booking;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(target = "item.id", source = "item.id")
    @Mapping(target = "item.name", source = "item.name")
    @Mapping(target = "booker.id", source = "booker.id")
    BookingDto toDto(Booking booking);
}
