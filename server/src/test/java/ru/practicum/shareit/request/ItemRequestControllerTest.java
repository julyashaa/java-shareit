package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ItemRequestService itemRequestService;

    @Test
    void createShouldReturn200AndCallService() throws Exception {
        ItemRequestCreateDto request = new ItemRequestCreateDto();
        request.setDescription("Need a drill");

        ItemRequestDto response = new ItemRequestDto();
        response.setId(1L);
        response.setDescription("Need a drill");
        response.setCreated(LocalDateTime.of(2026, 1, 1, 10, 0));
        response.setItems(List.of());

        when(itemRequestService.create(eq(10L), any(ItemRequestCreateDto.class))).thenReturn(response);

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a drill"));

        verify(itemRequestService).create(eq(10L), any(ItemRequestCreateDto.class));
    }

    @Test
    void createShouldReturn500WhenNoHeader() throws Exception {
        ItemRequestCreateDto request = new ItemRequestCreateDto();
        request.setDescription("Need a drill");

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());

        verifyNoInteractions(itemRequestService);
    }

    @Test
    void getOwnShouldReturn200AndCallService() throws Exception {
        ItemRequestDto r1 = new ItemRequestDto();
        r1.setId(1L);
        ItemRequestDto r2 = new ItemRequestDto();
        r2.setId(2L);

        when(itemRequestService.getOwn(10L)).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/requests")
                        .header(USER_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(itemRequestService).getOwn(10L);
    }

    @Test
    void getOwnShouldReturn500WhenNoHeader() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());

        verifyNoInteractions(itemRequestService);
    }

    @Test
    void getOthersShouldReturn200AndCallService() throws Exception {
        when(itemRequestService.getOthers(10L)).thenReturn(List.of());

        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(itemRequestService).getOthers(10L);
    }

    @Test
    void getOthersShouldReturn500WhenNoHeader() throws Exception {
        mockMvc.perform(get("/requests/all"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());

        verifyNoInteractions(itemRequestService);
    }

    @Test
    void getByIdShouldReturn200AndCallService() throws Exception {
        ItemRequestDto response = new ItemRequestDto();
        response.setId(5L);
        response.setDescription("Need a drill");
        response.setCreated(LocalDateTime.of(2026, 1, 1, 10, 0));
        response.setItems(List.of());

        when(itemRequestService.getById(10L, 5L)).thenReturn(response);

        mockMvc.perform(get("/requests/5")
                        .header(USER_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.description").value("Need a drill"));

        verify(itemRequestService).getById(10L, 5L);
    }

    @Test
    void getByIdShouldReturn500WhenNoHeader() throws Exception {
        mockMvc.perform(get("/requests/5"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());

        verifyNoInteractions(itemRequestService);
    }

}
