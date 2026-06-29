package com.nutricare.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutricare.exception.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IpfsServiceTest {

    @Mock private WebClient webClient;

    private ObjectMapper objectMapper;
    private IpfsService ipfsService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        ipfsService = new IpfsService(webClient, objectMapper);
        setField(ipfsService, "pinataApiKey", "test-key");
        setField(ipfsService, "pinataSecretKey", "test-secret");
        setField(ipfsService, "pinataApiUrl", "https://test.pinata.cloud");
        setField(ipfsService, "pinataGatewayUrl", "https://test.gateway.pinata.cloud/ipfs");
    }

    private void setField(Object target, String name, String value) {
        try {
            java.lang.reflect.Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void uploadJson_shouldReturnCid() {
        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        String mockResponse = "{\"IpfsHash\":\"QmTest123Hash\",\"PinSize\":100}";

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.header(anyString(), anyString())).thenReturn(bodySpec);
        when(bodySpec.contentType(any())).thenReturn(bodySpec);
        when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(reactor.core.publisher.Mono.just(mockResponse));

        String cid = ipfsService.uploadJson(Map.of("test", "data"));

        assertEquals("QmTest123Hash", cid);
    }

    @SuppressWarnings("unchecked")
    @Test
    void uploadJson_shouldThrow_onError() {
        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenThrow(new RuntimeException("Pinata error"));

        assertThrows(StorageException.class,
            () -> ipfsService.uploadJson(Map.of("test", "data")));
    }
}
