package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;
import ru.practicum.booking.client.BookingClient;
import ru.practicum.booking.dto.BookItemRequestDto;
import ru.practicum.booking.dto.BookingState;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class BookingClientTest {

    private BookingClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class, Answers.RETURNS_SELF);

        when(builder.uriTemplateHandler(any(UriTemplateHandler.class))).thenReturn(builder);
        when(builder.requestFactory(ArgumentMatchers.<Supplier<ClientHttpRequestFactory>>any())).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);

        ResponseEntity<Object> ok = ResponseEntity.ok().body(new Object());

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ok);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class), anyMap()))
                .thenReturn(ok);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class), any(Object[].class)))
                .thenReturn(ok);
        when(restTemplate.exchange(any(RequestEntity.class), eq(Object.class)))
                .thenReturn(ok);

        client = new BookingClient("http://localhost:9090", builder);
    }

    @Test
    void getBookingDoesNotThrow() {
        client.getBooking(1L, 1L);
    }

    @Test
    void getBookingsDoesNotThrow() {
        client.getBookings(1L, BookingState.ALL, 0, 10);
    }

    @Test
    void getOwnerBookingsDoesNotThrow() {
        client.getOwnerBookings(1L, BookingState.ALL, 0, 10);
    }

    @Test
    void approveDoesNotThrow() {
        client.approve(1L, 1L, true);
    }

    @Test
    void cancelDoesNotThrow() {
        client.cancel(1L, 1L);
    }

    @Test
    void bookItemDoesNotThrow() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);

        BookItemRequestDto dto = new BookItemRequestDto(1L, start, end);

        client.bookItem(1L, dto);
    }
}