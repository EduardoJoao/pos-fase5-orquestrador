package com.core.adapters.gateway;

import com.core.domain.core.model.VideoRequset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
public class CreateVideoCoreApiClientImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CreateVideoCoreApiClientImpl createVideoCoreApiClientImpl;

    private final String clientId = "user-123";
    private final String filename = "test-video.mp4";
    private final String uuid = "abc-123";
    private final String sizeVideo = "1.5 MB";
    
    @BeforeEach
    void setUp() {
        try {
            var field = createVideoCoreApiClientImpl.getClass().getDeclaredField("coreApiUrl");
            field.setAccessible(true);
            field.set(createVideoCoreApiClientImpl, "http://test-api.com");
        } catch (Exception e) {
            fail("Failed to set coreApiUrl field");
        }
    }

    @Test
    void createVideo_ShouldPostVideoDataSuccessfully() {
        when(restTemplate.postForEntity(
            anyString(),
            any(VideoRequset.class),
            eq(Void.class)
        )).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));
        
        createVideoCoreApiClientImpl.createVideo(clientId, filename, uuid, sizeVideo);
        
        ArgumentCaptor<VideoRequset> requestCaptor = ArgumentCaptor.forClass(VideoRequset.class);
        verify(restTemplate).postForEntity(
            contains("/videos"),
            requestCaptor.capture(),
            eq(Void.class)
        );
        
        VideoRequset capturedRequest = requestCaptor.getValue();
        assertEquals(clientId, capturedRequest.getUserId());
        assertEquals(uuid, capturedRequest.getVideoId());
        assertEquals(filename, capturedRequest.getVideoKey());
        assertEquals(sizeVideo, capturedRequest.getVideoKeySize());
    }
    
    @Test
    void createVideo_WhenApiReturnsError_ShouldThrowRuntimeException() {
        when(restTemplate.postForEntity(
            anyString(),
            any(VideoRequset.class),
            eq(Void.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        
        Exception exception = assertThrows(RuntimeException.class, () ->
            createVideoCoreApiClientImpl.createVideo(clientId, filename, uuid, sizeVideo)
        );
        
        assertTrue(exception.getMessage().contains("Failed to validate video"));
        
        verify(restTemplate).postForEntity(
            anyString(),
            any(VideoRequset.class),
            eq(Void.class)
        );
    }
}