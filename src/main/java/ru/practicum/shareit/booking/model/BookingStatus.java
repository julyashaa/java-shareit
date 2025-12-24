package ru.practicum.shareit.booking.model;

public enum BookingStatus {
    WAITING,   // создано, ждёт подтверждения владельцем
    APPROVED,  // подтверждено
    REJECTED,  // отклонено
    CANCELED   // отменено пользователем
}
