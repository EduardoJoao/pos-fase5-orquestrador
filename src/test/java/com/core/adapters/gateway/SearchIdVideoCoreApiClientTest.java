package com.core.adapters.gateway;

import com.core.domain.core.model.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SearchIdVideoCoreApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SearchIdVideoCoreApiClient searchIdVideoCoreApiClient;

    private final String videoId = "video-123";
    private Video mockVideo;

    @BeforeEach
    void setUp() {
        mockVideo = new Video();
        mockVideo.setId(videoId);
        mockVideo.setStatus("COMPLETED");
        
        // Configurando variável de ambiente para teste
        try {
            var field = searchIdVideoCoreApiClient.getClass().getDeclaredField("coreApiUrl");
            field.setAccessible(true);
            field.set(searchIdVideoCoreApiClient, "http://test-api.com");
        } catch (Exception e) {
            fail("Failed to set coreApiUrl field");
        }
    }

    @Test
    void validateVideo_ShouldReturnVideoWhenFound() {
        // Arrange
        when(restTemplate.getForEntity(
            contains("/videos/" + videoId),
            eq(Video.class)
        )).thenReturn(new ResponseEntity<>(mockVideo, HttpStatus.OK));
        
        // Act
        Video result = searchIdVideoCoreApiClient.validateVideo(videoId);
        
        // Assert
        assertNotNull(result);
        assertEquals(videoId, result.getId());
        assertEquals("COMPLETED", result.getStatus());
        verify(restTemplate).getForEntity(anyString(), eq(Video.class));
    }
    
    @Test
    void validateVideo_WhenApiReturnsNotFound_ShouldThrowRuntimeException() {
        // Arrange
        when(restTemplate.getForEntity(
            contains("/videos/" + videoId),
            eq(Video.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> 
            searchIdVideoCoreApiClient.validateVideo(videoId)
        );
        
        assertTrue(exception.getMessage().contains("Failed to validate video"));
        verify(restTemplate).getForEntity(anyString(), eq(Video.class));
    }
    
    @Test
    void validateVideo_WhenApiReturnsServerError_ShouldThrowRuntimeException() {
        // Arrange
        when(restTemplate.getForEntity(
            anyString(),
            eq(Video.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> 
            searchIdVideoCoreApiClient.validateVideo(videoId)
        );
        
        assertTrue(exception.getMessage().contains("Failed to validate video"));
        verify(restTemplate).getForEntity(anyString(), eq(Video.class));
    }
}