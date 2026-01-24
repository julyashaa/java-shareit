package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.user.client.UserClient;
import ru.practicum.user.controller.UserController;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserClient userClient;

    @Test
    void createShouldReturn200AndCallClient() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("name", "Юля", "email", "yulia@mail.com")
        );

        when(userClient.create(any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "name", "Юля", "email", "yulia@mail.com")));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(userClient).create(any());
    }

    @Test
    void createShouldReturn400WhenInvalidBody() throws Exception {
        String body = "{}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void updateShouldReturn200AndCallClient() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("name", "New", "email", "new@mail.com"));

        when(userClient.update(eq(5L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 5, "name", "New", "email", "new@mail.com")));

        mockMvc.perform(patch("/users/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        verify(userClient).update(eq(5L), any());
    }

    @Test
    void updateShouldReturn400WhenInvalidBody() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "not-email")
        );

        mockMvc.perform(patch("/users/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userClient);
    }

    @Test
    void getByIdShouldReturn200AndCallClient() throws Exception {
        when(userClient.getById(7L)).thenReturn(ResponseEntity.ok(Map.of("id", 7)));

        mockMvc.perform(get("/users/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));

        verify(userClient).getById(7L);
    }

    @Test
    void getAllShouldReturn200AndCallClient() throws Exception {
        when(userClient.getAll()).thenReturn(ResponseEntity.ok("[]"));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());

        verify(userClient).getAll();
    }

    @Test
    void deleteShouldReturn200AndCallClient() throws Exception {
        when(userClient.delete(3L)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/3"))
                .andExpect(status().isOk());

        verify(userClient).delete(3L);
    }
}