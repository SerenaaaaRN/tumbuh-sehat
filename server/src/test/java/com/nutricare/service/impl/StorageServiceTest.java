package com.nutricare.service.impl;

import com.nutricare.exception.StorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock private WebClient webClient;

    private StorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new StorageService(webClient);
        setField(storageService, "supabaseUrl", "https://test.supabase.co");
        setField(storageService, "supabaseKey", "test-key");
        setField(storageService, "bucket", "test-bucket");
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
    void upload_shouldReturnPublicUrl() {
        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(bodySpec);
        when(bodySpec.header(anyString(), anyString())).thenReturn(bodySpec);
        when(bodySpec.contentType(any())).thenReturn(bodySpec);
        when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(reactor.core.publisher.Mono.empty());

        String result = storageService.upload("test-data".getBytes(), "photo.jpg", "image/jpeg");

        assertNotNull(result);
        assertTrue(result.startsWith("https://test.supabase.co/storage/v1/object/public/test-bucket/"));
        assertTrue(result.endsWith(".jpg"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void upload_shouldThrow_onError() {
        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenThrow(new RuntimeException("Upload failed"));

        assertThrows(StorageException.class,
            () -> storageService.upload("test".getBytes(), "test.jpg", "image/jpeg"));
    }
}
