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
public class SearchIdVideoCoreApiClientImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SearchIdVideoCoreApiClientImpl searchIdVideoCoreApiClientImpl;

    private final String videoId = "video-123";
    private Video mockVideo;

    @BeforeEach
    void setUp() {
        mockVideo = new Video();
        mockVideo.setId(videoId);
        mockVideo.setStatus("COMPLETED");
        
        try {
            var field = searchIdVideoCoreApiClientImpl.getClass().getDeclaredField("coreApiUrl");
            field.setAccessible(true);
            field.set(searchIdVideoCoreApiClientImpl, "http://test-api.com");
        } catch (Exception e) {
            fail("Failed to set coreApiUrl field");
        }
    }

    @Test
    void validateVideo_ShouldReturnVideoWhenFound() {
        when(restTemplate.getForEntity(
            contains("/videos/" + videoId),
            eq(Video.class)
        )).thenReturn(new ResponseEntity<>(mockVideo, HttpStatus.OK));
        
        Video result = searchIdVideoCoreApiClientImpl.validateVideo(videoId);
        
        assertNotNull(result);
        assertEquals(videoId, result.getId());
        assertEquals("COMPLETED", result.getStatus());
        verify(restTemplate).getForEntity(anyString(), eq(Video.class));
    }
    
    @Test
    void validateVideo_WhenApiReturnsNotFound_ShouldThrowRuntimeException() {
        when(restTemplate.getForEntity(
            contains("/videos/" + videoId),
            eq(Video.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        
        Exception exception = assertThrows(RuntimeException.class, () ->
            searchIdVideoCoreApiClientImpl.validateVideo(videoId)
        );
        
        assertTrue(exception.getMessage().contains("Failed to validate video"));
        verify(restTemplate).getForEntity(anyString(), eq(Video.class));
    }
    
    @Test
    void validateVideo_WhenApiReturnsServerError_ShouldThrowRuntimeException() {
        when(restTemplate.getForEntity(
            anyString(),
            eq(Video.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        
        Exception exception = assertThrows(RuntimeException.class, () ->
            searchIdVideoCoreApiClientImpl.validateVideo(videoId)
        );
        
        assertTrue(exception.getMessage().contains("Failed to validate video"));
        verify(restTemplate).getForEntity(anyString(), eq(Video.class));
    }
}