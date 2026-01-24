package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingCreateRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Autowired
    BookingControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    void createShouldReturn200AndCallService() throws Exception {
        BookingCreateRequestDto request = new BookingCreateRequestDto();
        request.setItemId(1L);
        request.setStart(LocalDateTime.of(2026, 1, 20, 10, 0));
        request.setEnd(LocalDateTime.of(2026, 1, 21, 10, 0));

        BookingDto response = new BookingDto();
        response.setId(100L);
        response.setStatus(BookingStatus.WAITING);

        when(bookingService.create(eq(10L), any(BookingCreateRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService).create(eq(10L), any(BookingCreateRequestDto.class));
    }

    @Test
    void createShouldReturn500WhenNoHeader() throws Exception {
        BookingCreateRequestDto request = new BookingCreateRequestDto();
        request.setItemId(1L);
        request.setStart(LocalDateTime.of(2026, 1, 20, 10, 0));
        request.setEnd(LocalDateTime.of(2026, 1, 21, 10, 0));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());

        verifyNoInteractions(bookingService);
    }

    @Test
    void approveShouldReturn200AndCallService() throws Exception {
        BookingDto response = new BookingDto();
        response.setId(5L);
        response.setStatus(BookingStatus.APPROVED);

        when(bookingService.approve(10L, 5L, true)).thenReturn(response);

        mockMvc.perform(patch("/bookings/5")
                        .header(USER_HEADER, "10")
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService).approve(10L, 5L, true);
    }

    @Test
    void approveShouldReturn500WhenNoHeader() throws Exception {
        mockMvc.perform(patch("/bookings/5")
                        .param("approved", "true"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getByIdShouldReturn200AndCallService() throws Exception {
        BookingDto response = new BookingDto();
        response.setId(7L);
        response.setStatus(BookingStatus.WAITING);

        when(bookingService.getById(10L, 7L)).thenReturn(response);

        mockMvc.perform(get("/bookings/7")
                        .header(USER_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService).getById(10L, 7L);
    }

    @Test
    void getByBookerShouldReturn200DefaultStateALL_andCallService() throws Exception {
        BookingDto b1 = new BookingDto();
        b1.setId(1L);
        BookingDto b2 = new BookingDto();
        b2.setId(2L);

        when(bookingService.getByBooker(10L, "ALL")).thenReturn(List.of(b1, b2));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(bookingService).getByBooker(10L, "ALL");
    }

    @Test
    void getByBookerShouldReturn200WithStateParam_andCallService() throws Exception {
        when(bookingService.getByBooker(10L, "PAST")).thenReturn(List.of());

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, "10")
                        .param("state", "PAST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(bookingService).getByBooker(10L, "PAST");
    }

    @Test
    void getByOwnerShouldReturn200DefaultStateALLAndCallService() throws Exception {
        when(bookingService.getByOwner(10L, "ALL")).thenReturn(List.of());

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(bookingService).getByOwner(10L, "ALL");
    }

    @Test
    void getByOwnerShouldReturn200WithStateParamAndCallService() throws Exception {
        when(bookingService.getByOwner(10L, "FUTURE")).thenReturn(List.of());

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, "10")
                        .param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(bookingService).getByOwner(10L, "FUTURE");
    }

    @Test
    void cancelShouldReturn200AndCallService() throws Exception {
        BookingDto response = new BookingDto();
        response.setId(9L);
        response.setStatus(BookingStatus.CANCELED);

        when(bookingService.cancel(10L, 9L)).thenReturn(response);

        mockMvc.perform(patch("/bookings/9/cancel")
                        .header(USER_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.status").value("CANCELED"));

        verify(bookingService).cancel(10L, 9L);
    }

    @Test
    void cancelShouldReturn500WhenNoHeader() throws Exception {
        mockMvc.perform(patch("/bookings/9/cancel"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());

        verifyNoInteractions(bookingService);
    }
}
