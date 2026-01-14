package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;
import ru.practicum.item.client.ItemClient;
import ru.practicum.item.dto.CommentCreateDto;
import ru.practicum.item.dto.ItemCreateDto;
import ru.practicum.item.dto.ItemUpdateDto;

import java.util.function.Supplier;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class ItemClientTest {

    private MockRestServiceServer server;
    private ItemClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();

        RestTemplateBuilder builder = new RestTemplateBuilder() {
            @Override
            public RestTemplateBuilder uriTemplateHandler(UriTemplateHandler handler) {
                restTemplate.setUriTemplateHandler(handler);
                return this;
            }

            @Override
            public RestTemplateBuilder requestFactory(Supplier<ClientHttpRequestFactory> requestFactorySupplier) {
                return this;
            }

            @Override
            public RestTemplate build() {
                return restTemplate;
            }
        };

        client = new ItemClient("http://localhost:9090", builder);
    }

    @Test
    void addShouldPostToItemsWithUserHeader() {
        long userId = 1L;

        server.expect(once(), requestTo("http://localhost:9090/items"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", String.valueOf(userId)))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.add(userId, new ItemCreateDto());

        server.verify();
    }

    @Test
    void updateShouldPatchItemByIdWithUserHeader() {
        long userId = 1L;
        long itemId = 2L;

        server.expect(once(), requestTo("http://localhost:9090/items/" + itemId))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("X-Sharer-User-Id", String.valueOf(userId)))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.update(userId, itemId, new ItemUpdateDto());

        server.verify();
    }

    @Test
    void getByIdShouldGetItemByIdWithUserHeader() {
        long userId = 1L;
        long itemId = 2L;

        server.expect(once(), requestTo("http://localhost:9090/items/" + itemId))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", String.valueOf(userId)))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.getById(userId, itemId);

        server.verify();
    }

    @Test
    void getAllShouldGetItemsWithUserHeader() {
        long userId = 1L;

        server.expect(once(), requestTo("http://localhost:9090/items"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", String.valueOf(userId)))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        client.getAll(userId);

        server.verify();
    }

    @Test
    void searchShouldGetSearchWithoutUserHeader() {
        String text = "drill";

        server.expect(once(), requestTo("http://localhost:9090/items/search?text=" + text))
                .andExpect(method(HttpMethod.GET))
                .andExpect(headerDoesNotExist("X-Sharer-User-Id"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        client.search(text);

        server.verify();
    }

    @Test
    void addCommentShouldPostCommentWithUserHeader() {
        long userId = 1L;
        long itemId = 2L;

        server.expect(once(), requestTo("http://localhost:9090/items/" + itemId + "/comment"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", String.valueOf(userId)))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.addComment(userId, itemId, new CommentCreateDto());

        server.verify();
    }
}
