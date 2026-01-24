package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.request.ItemRequestController;
import ru.practicum.request.client.ItemRequestClient;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ItemRequestClient itemRequestClient;

    @Test
    void createShouldReturn200AndCallClient() throws Exception {
        String body = "{\"description\":\"Need a drill\"}";

        when(itemRequestClient.create(eq(10L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "description", "Need a drill", "items", List.of())));

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(itemRequestClient).create(eq(10L), any());
    }

    @Test
    void createShouldReturn400WhenNoHeader() throws Exception {
        String body = "{\"description\":\"Need a drill\"}";

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void createShouldReturn400WhenMalformedJson() throws Exception {
        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void getOwnShouldReturn200AndCallClient() throws Exception {
        when(itemRequestClient.getOwn(10L)).thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1), Map.of("id", 2))));

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(itemRequestClient).getOwn(10L);
    }

    @Test
    void getOwnShouldReturn400WhenNoHeader() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void getOthersShouldReturn200AndCallClient() throws Exception {
        when(itemRequestClient.getOthers(10L)).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(itemRequestClient).getOthers(10L);
    }

    @Test
    void getOthersShouldReturn400WhenNoHeader() throws Exception {
        mockMvc.perform(get("/requests/all"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void getByIdShouldReturn200AndCallClient() throws Exception {
        when(itemRequestClient.getById(10L, 5L)).thenReturn(ResponseEntity.ok(Map.of("id", 5)));

        mockMvc.perform(get("/requests/5")
                        .header(USER_ID_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        verify(itemRequestClient).getById(10L, 5L);
    }

    @Test
    void getByIdShouldReturn400WhenNoHeader() throws Exception {
        mockMvc.perform(get("/requests/5"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }
}
