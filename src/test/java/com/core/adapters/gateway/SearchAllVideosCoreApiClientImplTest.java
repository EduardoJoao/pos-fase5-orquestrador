package com.core.adapters.gateway;

import com.core.domain.core.model.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SearchAllVideosCoreApiClientImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SearchAllVideosCoreApiClientImpl searchAllVideosCoreApiClientImpl;

    private final String userEmail = "user@example.com";
    private List<Video> mockVideos;

    @BeforeEach
    void setUp() {
        Video video1 = new Video();
        video1.setId("1");
        video1.setStatus("COMPLETED");
        
        Video video2 = new Video();
        video2.setId("2");
        video2.setStatus("PROCESSING");
        
        mockVideos = Arrays.asList(video1, video2);
        
        try {
            var field = searchAllVideosCoreApiClientImpl.getClass().getDeclaredField("coreApiUrl");
            field.setAccessible(true);
            field.set(searchAllVideosCoreApiClientImpl, "http://test-api.com");
        } catch (Exception e) {
            fail("Failed to set coreApiUrl field");
        }
    }

    @Test
    void getAllVideos_ShouldReturnListOfVideos() {
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(mockVideos, HttpStatus.OK));
        
        List<Video> result = searchAllVideosCoreApiClientImpl.getAllVideos(userEmail);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals("COMPLETED", result.get(0).getStatus());
        assertEquals("2", result.get(1).getId());
        
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
            eq("http://test-api.com/videos"),
            eq(HttpMethod.GET),
            entityCaptor.capture(),
            any(ParameterizedTypeReference.class)
        );
        
        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        assertNotNull(capturedEntity.getHeaders());
        assertEquals(userEmail, capturedEntity.getHeaders().getFirst("idClient"));
    }
    
    @Test
    void getAllVideos_WhenApiReturnsError_ShouldThrowRuntimeException() {
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        
        Exception exception = assertThrows(RuntimeException.class, () ->
            searchAllVideosCoreApiClientImpl.getAllVideos(userEmail)
        );
        
        assertTrue(exception.getMessage().contains("Failed to retrieve videos"));
        
        verify(restTemplate).exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        );
    }
    
    @Test
    void getAllVideos_WhenApiReturnsEmptyList_ShouldReturnEmptyList() {
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(List.of(), HttpStatus.OK));
        
        List<Video> result = searchAllVideosCoreApiClientImpl.getAllVideos(userEmail);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(restTemplate).exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void getAllVideos_ShouldIncludeCorrectIdClientHeader() {
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(mockVideos, HttpStatus.OK));
        
        searchAllVideosCoreApiClientImpl.getAllVideos(userEmail);
        
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
            eq("http://test-api.com/videos"),
            eq(HttpMethod.GET),
            entityCaptor.capture(),
            any(ParameterizedTypeReference.class)
        );
        
        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        assertEquals("user@example.com", capturedEntity.getHeaders().getFirst("idClient"));
    }
}