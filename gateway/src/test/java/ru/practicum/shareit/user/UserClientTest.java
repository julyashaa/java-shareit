package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;
import ru.practicum.user.client.UserClient;
import ru.practicum.user.dto.UserCreateDto;
import ru.practicum.user.dto.UserUpdateDto;

import java.util.function.Supplier;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class UserClientTest {

    private MockRestServiceServer server;
    private UserClient client;

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

        client = new UserClient("http://localhost:9090", builder);
    }

    @Test
    void createShouldPostToUsersWithoutUserHeader() {
        server.expect(once(), requestTo("http://localhost:9090/users"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(headerDoesNotExist("X-Sharer-User-Id"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.create(new UserCreateDto());

        server.verify();
    }

    @Test
    void updateShouldPatchUserByIdWithoutUserHeader() {
        long userId = 1L;

        server.expect(once(), requestTo("http://localhost:9090/users/" + userId))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(headerDoesNotExist("X-Sharer-User-Id"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.update(userId, new UserUpdateDto());

        server.verify();
    }

    @Test
    void getByIdShouldGetUserByIdWithoutUserHeader() {
        long userId = 1L;

        server.expect(once(), requestTo("http://localhost:9090/users/" + userId))
                .andExpect(method(HttpMethod.GET))
                .andExpect(headerDoesNotExist("X-Sharer-User-Id"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        client.getById(userId);

        server.verify();
    }

    @Test
    void getAllShouldGetUsersWithoutUserHeader() {
        server.expect(once(), requestTo("http://localhost:9090/users"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(headerDoesNotExist("X-Sharer-User-Id"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        client.getAll();

        server.verify();
    }

    @Test
    void deleteShouldDeleteUserByIdWithoutUserHeader() {
        long userId = 1L;

        server.expect(once(), requestTo("http://localhost:9090/users/" + userId))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(headerDoesNotExist("X-Sharer-User-Id"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        client.delete(userId);

        server.verify();
    }
}