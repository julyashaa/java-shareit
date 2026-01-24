package ru.practicum.shareit.request;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;
import ru.practicum.request.client.ItemRequestClient;
import ru.practicum.request.dto.ItemRequestDto;

import java.util.function.Supplier;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ItemRequestClientTest {

    private MockRestServiceServer server;
    private ItemRequestClient client;

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
            public RestTemplateBuilder requestFactory(Supplier<ClientHttpRequestFactory> supplier) {
                return this;
            }

            @Override
            public RestTemplate build() {
                return restTemplate;
            }
        };

        client = new ItemRequestClient("http://localhost:9090", builder);
    }

    @Test
    void createShouldPostToRequestsWithUserHeader() {
        long userId = 1L;

        server.expect(once(), requestTo("http://localhost:9090/requests"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", String.valueOf(userId)))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.create(userId, new ItemRequestDto());

        server.verify();
    }

    @Test
    void getOwnShouldGetRequestsWithUserHeader() {
        long userId = 1L;

        server.expect(once(), requestTo("http://localhost:9090/requests"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", String.valueOf(userId)))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        client.getOwn(userId);

        server.verify();
    }

    @Test
    void getOthersShouldGetAllRequestsWithUserHeader() {
        long userId = 1L;

        server.expect(once(), requestTo("http://localhost:9090/requests/all"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", String.valueOf(userId)))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        client.getOthers(userId);

        server.verify();
    }

    @Test
    void getByIdShouldGetRequestByIdWithUserHeader() {
        long userId = 1L;
        long requestId = 10L;

        server.expect(once(), requestTo("http://localhost:9090/requests/" + requestId))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", String.valueOf(userId)))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.getById(userId, requestId);

        server.verify();
    }
}