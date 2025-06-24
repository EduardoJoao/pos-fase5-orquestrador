package com.core.domain.status.usecase;

import com.core.adapters.gateway.SearchAllVideosCoreApiClient;
import com.core.domain.core.model.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.StatusResponse;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SearchAllVideosUseCaseTest {

    @Mock
    private SearchAllVideosCoreApiClient searchAllVideosCoreApiClient;

    @InjectMocks
    private SearchAllVideosUseCase searchAllVideosUseCase;

    private final String clientEmail = "user@example.com";
    private List<Video> mockVideos;

    @BeforeEach
    void setUp() {
        // Setup mock videos
        Video video1 = new Video();
        video1.setId("1");
        video1.setUserId("user-1");
        video1.setVideoZipKey("video1.zip");
        video1.setVideoZipKeySize("1.5MB");
        video1.setStatus("COMPLETED");
        video1.setCreatedAt(LocalDateTime.now());

        Video video2 = new Video();
        video2.setId("2");
        video2.setUserId("user-1");
        video2.setVideoZipKey("video2.zip");
        video2.setVideoZipKeySize("2.1MB");
        video2.setStatus("PROCESSING");
        video2.setCreatedAt(LocalDateTime.now().minusDays(1));

        mockVideos = Arrays.asList(video1, video2);
    }

    @Test
    void execute_ShouldReturnStatusResponseWithVideos() {
        // Arrange
        when(searchAllVideosCoreApiClient.getAllVideos(clientEmail)).thenReturn(mockVideos);
        
        // Act
        StatusResponse result = searchAllVideosUseCase.execute(clientEmail);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getFiles().size());
        assertEquals("1", result.getFiles().get(0).getId());
        assertEquals("video1.zip", result.getFiles().get(0).getFilename());
        assertEquals("COMPLETED", result.getFiles().get(0).getStatus());
        
        verify(searchAllVideosCoreApiClient).getAllVideos(clientEmail);
    }
    
    @Test
    void execute_WhenApiClientThrowsException_ShouldReturnNull() {
        // Arrange
        when(searchAllVideosCoreApiClient.getAllVideos(clientEmail))
            .thenThrow(new RuntimeException("API Error"));
        
        // Act
        StatusResponse result = searchAllVideosUseCase.execute(clientEmail);
        
        // Assert
        assertNull(result);
        verify(searchAllVideosCoreApiClient).getAllVideos(clientEmail);
    }
    
    @Test
    void execute_WhenNoVideosFound_ShouldReturnEmptyResponse() {
        // Arrange
        when(searchAllVideosCoreApiClient.getAllVideos(clientEmail)).thenReturn(List.of());
        
        // Act
        StatusResponse result = searchAllVideosUseCase.execute(clientEmail);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getFiles().isEmpty());
        verify(searchAllVideosCoreApiClient).getAllVideos(clientEmail);
    }
}