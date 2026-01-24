package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.booking.BookingController;
import ru.practicum.booking.client.BookingClient;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean BookingClient bookingClient;

    @Test
    void getBookingsShouldReturn200AndCallClient_withDefaults() throws Exception {
        when(bookingClient.getBookings(eq(10L), any(), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1))));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(bookingClient).getBookings(eq(10L), any(), eq(0), eq(10));
    }

    @Test
    void getBookingsShouldReturn200AndCallClient_withParams() throws Exception {
        when(bookingClient.getBookings(eq(10L), any(), eq(5), eq(20)))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, "10")
                        .param("state", "future")
                        .param("from", "5")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(bookingClient).getBookings(eq(10L), any(), eq(5), eq(20));
    }

    @Test
    void getBookingsShouldReturn400WhenNoHeader() throws Exception {
        mockMvc.perform(get("/bookings"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void getBookingsShouldReturn400WhenFromNegative() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, "10")
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void getBookingsShouldReturn400WhenSizeNotPositive() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, "10")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void getBookingsShouldReturn400WhenUnknownState() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, "10")
                        .param("state", "abracadabra"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void bookItemShouldReturn200AndCallClient() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "itemId", 1,
                        "start", "2026-01-20T10:00:00",
                        "end", "2026-01-21T10:00:00"
                )
        );

        when(bookingClient.bookItem(eq(10L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 100)));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));

        verify(bookingClient).bookItem(eq(10L), any());
    }

    @Test
    void bookItemShouldReturn400WhenNoHeader() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "itemId", 1,
                        "start", "2026-01-20T10:00:00",
                        "end", "2026-01-21T10:00:00"
                )
        );

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void bookItemShouldReturn400WhenInvalidBody() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void getBookingShouldReturn200AndCallClient() throws Exception {
        when(bookingClient.getBooking(10L, 7L)).thenReturn(ResponseEntity.ok(Map.of("id", 7)));

        mockMvc.perform(get("/bookings/7")
                        .header(USER_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));

        verify(bookingClient).getBooking(10L, 7L);
    }

    @Test
    void getBookingShouldReturn400WhenNoHeader() throws Exception {
        mockMvc.perform(get("/bookings/7"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void approveShouldReturn200AndCallClient() throws Exception {
        when(bookingClient.approve(10L, 5L, true)).thenReturn(ResponseEntity.ok(Map.of("id", 5, "status", "APPROVED")));

        mockMvc.perform(patch("/bookings/5")
                        .header(USER_HEADER, "10")
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        verify(bookingClient).approve(10L, 5L, true);
    }

    @Test
    void approveShouldReturn400WhenNoHeader() throws Exception {
        mockMvc.perform(patch("/bookings/5")
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void getOwnerBookingShouldReturn200AndCallClientWithDefaults() throws Exception {
        when(bookingClient.getOwnerBookings(eq(10L), any(), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, "10"))
                .andExpect(status().isOk());

        verify(bookingClient).getOwnerBookings(eq(10L), any(), eq(0), eq(10));
    }

    @Test
    void getOwnerBookingsShouldReturn400WhenUnknownState() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, "10")
                        .param("state", "nope"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void getOwnerBookingsShouldReturn400WhenFromNegative() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, "10")
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void getOwnerBookingsShouldReturn400WhenSizeNotPositive() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, "10")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void cancelShouldReturn200AndCallClient() throws Exception {
        when(bookingClient.cancel(10L, 9L)).thenReturn(ResponseEntity.ok(Map.of("id", 9, "status", "CANCELED")));

        mockMvc.perform(patch("/bookings/9/cancel")
                        .header(USER_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9));

        verify(bookingClient).cancel(10L, 9L);
    }

    @Test
    void cancelShouldReturn400WhenNoHeader() throws Exception {
        mockMvc.perform(patch("/bookings/9/cancel"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }
}
