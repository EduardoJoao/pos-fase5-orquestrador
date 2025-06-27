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
public class SearchAllVideosCoreApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SearchAllVideosCoreApiClient searchAllVideosCoreApiClient;

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
        
        // Configurando variável de ambiente para teste
        try {
            var field = searchAllVideosCoreApiClient.getClass().getDeclaredField("coreApiUrl");
            field.setAccessible(true);
            field.set(searchAllVideosCoreApiClient, "http://test-api.com");
        } catch (Exception e) {
            fail("Failed to set coreApiUrl field");
        }
    }

    @Test
    void getAllVideos_ShouldReturnListOfVideos() {
        // Arrange
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),  // Mudado de isNull() para any(HttpEntity.class)
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(mockVideos, HttpStatus.OK));
        
        // Act
        List<Video> result = searchAllVideosCoreApiClient.getAllVideos(userEmail);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals("COMPLETED", result.get(0).getStatus());
        assertEquals("2", result.get(1).getId());
        
        // Verificar se o método foi chamado com HttpEntity contendo o header
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
            eq("http://test-api.com/videos"),
            eq(HttpMethod.GET),
            entityCaptor.capture(),
            any(ParameterizedTypeReference.class)
        );
        
        // Verificar se o header idClient foi incluído
        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        assertNotNull(capturedEntity.getHeaders());
        assertEquals(userEmail, capturedEntity.getHeaders().getFirst("idClient"));
    }
    
    @Test
    void getAllVideos_WhenApiReturnsError_ShouldThrowRuntimeException() {
        // Arrange
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),  // Mudado de isNull() para any(HttpEntity.class)
            any(ParameterizedTypeReference.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> 
            searchAllVideosCoreApiClient.getAllVideos(userEmail)
        );
        
        assertTrue(exception.getMessage().contains("Failed to retrieve videos"));
        
        verify(restTemplate).exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),  // Mudado de isNull() para any(HttpEntity.class)
            any(ParameterizedTypeReference.class)
        );
    }
    
    @Test
    void getAllVideos_WhenApiReturnsEmptyList_ShouldReturnEmptyList() {
        // Arrange
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),  // Mudado de isNull() para any(HttpEntity.class)
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(List.of(), HttpStatus.OK));
        
        // Act
        List<Video> result = searchAllVideosCoreApiClient.getAllVideos(userEmail);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(restTemplate).exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),  // Mudado de isNull() para any(HttpEntity.class)
            any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void getAllVideos_ShouldIncludeCorrectIdClientHeader() {
        // Arrange
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)
        )).thenReturn(new ResponseEntity<>(mockVideos, HttpStatus.OK));
        
        // Act
        searchAllVideosCoreApiClient.getAllVideos(userEmail);
        
        // Assert
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
            eq("http://test-api.com/videos"),
            eq(HttpMethod.GET),
            entityCaptor.capture(),
            any(ParameterizedTypeReference.class)
        );
        
        // Verificar se o header idClient contém o email correto
        HttpEntity<?> capturedEntity = entityCaptor.getValue();
        assertEquals("user@example.com", capturedEntity.getHeaders().getFirst("idClient"));
    }
}