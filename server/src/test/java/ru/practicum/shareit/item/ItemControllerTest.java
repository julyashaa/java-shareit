package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void addShouldReturn200AndCallService() throws Exception {
        ItemDto request = new ItemDto();
        request.setName("Drill");
        request.setDescription("Powerful");
        request.setAvailable(true);

        ItemDto response = new ItemDto();
        response.setId(1L);
        response.setName("Drill");
        response.setDescription("Powerful");
        response.setAvailable(true);

        when(itemService.add(eq(10L), any(ItemDto.class))).thenReturn(response);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.description").value("Powerful"))
                .andExpect(jsonPath("$.available").value(true));

        ArgumentCaptor<ItemDto> captor = ArgumentCaptor.forClass(ItemDto.class);
        verify(itemService).add(eq(10L), captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Drill");
    }

    @Test
    void addShouldReturn500WhenNoHeader() throws Exception {
        ItemDto request = new ItemDto();
        request.setName("Drill");

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());

        verifyNoInteractions(itemService);
    }

    @Test
    void updateShouldReturn200AndCallService() throws Exception {
        ItemDto request = new ItemDto();
        request.setName("Updated");

        ItemDto response = new ItemDto();
        response.setId(5L);
        response.setName("Updated");

        when(itemService.update(eq(10L), eq(5L), any(ItemDto.class))).thenReturn(response);

        mockMvc.perform(patch("/items/5")
                        .header(USER_ID_HEADER, "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Updated"));

        verify(itemService).update(eq(10L), eq(5L), any(ItemDto.class));
    }

    @Test
    void getByIdShouldReturn200AndCallService() throws Exception {
        ItemDto response = new ItemDto();
        response.setId(7L);
        response.setName("Drill");

        when(itemService.getById(10L, 7L)).thenReturn(response);

        mockMvc.perform(get("/items/7")
                        .header(USER_ID_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Drill"));

        verify(itemService).getById(10L, 7L);
    }

    @Test
    void getAllShouldReturn200AndCallService() throws Exception {
        ItemDto i1 = new ItemDto();
        i1.setId(1L);
        i1.setName("Drill");

        ItemDto i2 = new ItemDto();
        i2.setId(2L);
        i2.setName("Hammer");

        when(itemService.getAll(10L)).thenReturn(List.of(i1, i2));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(itemService).getAll(10L);
    }

    @Test
    void searchShouldReturn200AndCallServiceWithText() throws Exception {
        ItemDto i1 = new ItemDto();
        i1.setId(1L);
        i1.setName("Drill");

        when(itemService.search("drill")).thenReturn(List.of(i1));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Drill"));

        verify(itemService).search("drill");
    }

    @Test
    void searchShouldReturn200AndCallServiceWithNoTextParam() throws Exception {
        when(itemService.search(null)).thenReturn(List.of());

        mockMvc.perform(get("/items/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(itemService).search(null);
    }

    @Test
    void addCommentShouldReturn200AndCallService() throws Exception {
        CommentCreateDto request = new CommentCreateDto();
        request.setText("Great!");

        CommentDto response = new CommentDto();
        response.setId(99L);
        response.setText("Great!");

        when(itemService.addComment(eq(10L), eq(5L), any(CommentCreateDto.class))).thenReturn(response);

        mockMvc.perform(post("/items/5/comment")
                        .header(USER_ID_HEADER, "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.text").value("Great!"));

        verify(itemService).addComment(eq(10L), eq(5L), any(CommentCreateDto.class));
    }

    @Test
    void addCommentShouldReturn500WhenNoHeader() throws Exception {
        CommentCreateDto request = new CommentCreateDto();
        request.setText("Great!");

        mockMvc.perform(post("/items/5/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());

        verifyNoInteractions(itemService);
    }



}
