package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createShouldReturn200AndCallService() throws Exception {
        UserDto request = new UserDto();
        request.setName("Юля");
        request.setEmail("yulia@mail.com");

        UserDto response = new UserDto();
        response.setId(1L);
        response.setName("Юля");
        response.setEmail("yulia@mail.com");

        when(userService.create(any(UserDto.class))).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Юля"))
                .andExpect(jsonPath("$.email").value("yulia@mail.com"));

        verify(userService).create(any(UserDto.class));
    }

    @Test
    void updateShouldReturn200AndCallService() throws Exception {
        UserDto request = new UserDto();
        request.setName("New");

        UserDto response = new UserDto();
        response.setId(5L);
        response.setName("New");
        response.setEmail("old@mail.com");

        when(userService.update(eq(5L), any(UserDto.class))).thenReturn(response);

        mockMvc.perform(patch("/users/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("New"))
                .andExpect(jsonPath("$.email").value("old@mail.com"));

        verify(userService).update(eq(5L), any(UserDto.class));
    }

    @Test
    void getByIdShouldReturn200AndCallService() throws Exception {
        UserDto response = new UserDto();
        response.setId(7L);
        response.setName("User");
        response.setEmail("user@mail.com");

        when(userService.getById(7L)).thenReturn(response);

        mockMvc.perform(get("/users/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("User"))
                .andExpect(jsonPath("$.email").value("user@mail.com"));

        verify(userService).getById(7L);
    }

    @Test
    void getAllShouldReturn200AndCallService() throws Exception {
        UserDto u1 = new UserDto();
        u1.setId(1L);
        u1.setName("A");

        UserDto u2 = new UserDto();
        u2.setId(2L);
        u2.setName("B");

        when(userService.getAll()).thenReturn(List.of(u1, u2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(userService).getAll();
    }

    @Test
    void deleteShouldReturn200AndCallService() throws Exception {
        doNothing().when(userService).delete(3L);

        mockMvc.perform(delete("/users/3"))
                .andExpect(status().isOk());

        verify(userService).delete(3L);
    }
}
