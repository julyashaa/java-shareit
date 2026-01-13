package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.item.ItemController;
import ru.practicum.item.client.ItemClient;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ItemClient itemClient;

    @Test
    void addShouldReturn200AndCallClient() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "name", "Drill",
                        "description", "Powerful",
                        "available", true
                )
        );

        when(itemClient.add(eq(10L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "name", "Drill")));

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(itemClient).add(eq(10L), any());
    }

    @Test
    void addShouldReturn400WhenNoHeader() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of(
                        "name", "Drill",
                        "description", "Powerful",
                        "available", true
                )
        );

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void addShouldReturn400WhenInvalidBody() throws Exception {
        String body = "{}";

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void updateShouldReturn200AndCallClient() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", "Updated"));

        when(itemClient.update(eq(10L), eq(5L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 5, "name", "Updated")));

        mockMvc.perform(patch("/items/5")
                        .header(USER_ID_HEADER, "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        verify(itemClient).update(eq(10L), eq(5L), any());
    }

    @Test
    void updateShouldReturn400WhenNoHeader() throws Exception {
        mockMvc.perform(patch("/items/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void getByIdShouldReturn200AndCallClient() throws Exception {
        when(itemClient.getById(10L, 7L)).thenReturn(ResponseEntity.ok(Map.of("id", 7)));

        mockMvc.perform(get("/items/7")
                        .header(USER_ID_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));

        verify(itemClient).getById(10L, 7L);
    }

    @Test
    void getByIdShouldReturn400WhenNoHeader() throws Exception {
        mockMvc.perform(get("/items/7"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void getAllShouldReturn200AndCallClient() throws Exception {
        when(itemClient.getAll(10L)).thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1), Map.of("id", 2))));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(itemClient).getAll(10L);
    }

    @Test
    void getAllShouldReturn400WhenNoHeader() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void searchShouldReturn200AndCallClientWhenTextProvided() throws Exception {
        when(itemClient.search("drill")).thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1))));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(itemClient).search("drill");
    }

    @Test
    void searchShouldReturn200AndEmptyListWhenTextBlankAndNotCallClient() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", "   "))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verifyNoInteractions(itemClient);
    }

    @Test
    void searchShouldReturn200AndEmptyListWhenTextMissingAndNotCallClient() throws Exception {
        mockMvc.perform(get("/items/search"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verifyNoInteractions(itemClient);
    }

    @Test
    void addCommentShouldReturn200AndCallClient() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("text", "Great!"));

        when(itemClient.addComment(eq(10L), eq(5L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 99, "text", "Great!")));

        mockMvc.perform(post("/items/5/comment")
                        .header(USER_ID_HEADER, "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.text").value("Great!"));

        verify(itemClient).addComment(eq(10L), eq(5L), any());
    }

    @Test
    void addCommentShouldReturn400WhenNoHeader() throws Exception {
        mockMvc.perform(post("/items/5/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Great!\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }

    @Test
    void addCommentShouldReturn400WhenInvalidBody() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("text", "   "));

        mockMvc.perform(post("/items/5/comment")
                        .header(USER_ID_HEADER, "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemClient);
    }
}

