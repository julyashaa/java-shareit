package ru.practicum.shareit;

import org.springframework.web.client.RestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import ru.practicum.BaseClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class BaseClientTest {
    private RestTemplate rest;
    private TestClient client;

    private static final String PATH = "/path";
    private static final String USERS = "/users";
    private static final String SEARCH = "/search?text={text}";
    private static final long USER_ID_10 = 10L;
    private static final long USER_ID_1 = 1L;

    @BeforeEach
    void setUp() {
        this.rest = mock(RestTemplate.class);
        this.client = new TestClient(this.rest);
    }

    @Test
    void getWithUserIdSetsHeader() {
        when(rest.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().body(Map.of("ok", true)));

        ResponseEntity<Object> response = client.getWithUser(PATH, USER_ID_10);
        assertThat(response).isNotNull();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest, times(1)).exchange(eq(PATH), eq(HttpMethod.GET), captor.capture(), eq(Object.class));

        HttpHeaders headers = captor.getValue().getHeaders();
        assertThat(headers.getFirst("X-Sharer-User-Id")).isEqualTo("10");
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(headers.getAccept()).contains(MediaType.APPLICATION_JSON);
    }

    @Test
    void getWithoutUserIdDoesNotSetHeader() {
        when(rest.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().body(Map.of("ok", true)));

        ResponseEntity<Object> response = client.getNoUser(PATH);
        assertThat(response).isNotNull();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest, times(1)).exchange(eq(PATH), eq(HttpMethod.GET), captor.capture(), eq(Object.class));

        HttpHeaders headers = captor.getValue().getHeaders();
        assertThat(headers.containsKey("X-Sharer-User-Id")).isFalse();
    }

    @Test
    void getWithParametersUsesExchangeWithUriVariables() {
        when(rest.exchange(anyString(), any(), any(), eq(Object.class), anyMap()))
                .thenReturn(ResponseEntity.ok().body(Map.of("ok", true)));

        ResponseEntity<Object> response = client.getWithParams(SEARCH, USER_ID_1, Map.of("text", "drill"));
        assertThat(response).isNotNull();

        verify(rest, times(1)).exchange(
                eq(SEARCH),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object.class),
                eq(Map.of("text", "drill"))
        );
    }

    @Test
    void whenServerReturns400BaseClientReturnsStatusAndBodyBytes() {
        byte[] body = "error".getBytes();

        when(rest.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.BAD_REQUEST,
                        "Bad Request",
                        HttpHeaders.EMPTY,
                        body,
                        null
                ));

        ResponseEntity<Object> resp = client.getNoUser("/bad");

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).isInstanceOf(byte[].class);
        assertThat((byte[]) resp.getBody()).isEqualTo(body);
    }

    @Test
    void whenNon200WithBodyReturnsStatusAndBody() {
        ResponseEntity<Object> fromServer = ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "not found"));

        when(rest.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(fromServer);

        ResponseEntity<Object> resp = client.getNoUser("/missing");

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resp.getBody()).isEqualTo(Map.of("message", "not found"));
    }

    @Test
    void postSendsBody() {
        when(rest.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().body(Map.of("ok", true)));

        Map<String, Object> body = Map.of("name", "Юля");

        ResponseEntity<Object> response = client.postNoUser(USERS, body);
        assertThat(response).isNotNull();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<?>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest, times(1)).exchange(eq(USERS), eq(HttpMethod.POST), captor.capture(), eq(Object.class));

        assertThat(captor.getValue().getBody()).isEqualTo(body);
    }

    static class TestClient extends BaseClient {
        TestClient(RestTemplate rest) {
            super(rest);
        }

        ResponseEntity<Object> getNoUser(String path) {
            return get(path);
        }

        ResponseEntity<Object> getWithUser(String path, long userId) {
            return get(path, userId);
        }

        ResponseEntity<Object> getWithParams(String path, long userId, Map<String, Object> params) {
            return get(path, userId, params);
        }

        ResponseEntity<Object> postNoUser(String path, Object body) {
            return post(path, body);
        }
    }
}
