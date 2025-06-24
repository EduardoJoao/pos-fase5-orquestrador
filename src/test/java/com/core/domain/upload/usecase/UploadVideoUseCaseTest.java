package com.core.domain.upload.usecase;

import com.core.adapters.gateway.CreateVideoCoreApiClient;
import com.core.domain.upload.activity.S3FileStorageService;
import com.core.domain.upload.activity.SQSMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UploadVideoUseCaseTest {

    @Mock
    private CreateVideoCoreApiClient createVideoCoreApiClient;

    @Mock
    private S3FileStorageService s3FileStorageService;

    @Mock
    private SQSMessageService sqsMessageService;

    @InjectMocks
    private UploadVideoUseCase uploadVideoUseCase;

    @Captor
    private ArgumentCaptor<Map<String, Object>> messageCaptor;

    private MultipartFile videoFile;
    private final String clientId = "user@example.com";

    @BeforeEach
    void setUp() {
        videoFile = new MockMultipartFile(
                "video",
                "test-video.mp4",
                "video/mp4",
                new byte[1024 * 1024] // 1MB para teste
        );
    }

    @Test
    void execute_ShouldProcessVideoUploadSuccessfully() throws IOException {
        // Act
        uploadVideoUseCase.execute(videoFile, clientId);
        
        // Assert
        verify(createVideoCoreApiClient).createVideo(
            eq(clientId), 
            eq(videoFile.getOriginalFilename()), 
            any(String.class), 
            eq("1,00 MB")
        );
        
        verify(s3FileStorageService).uploadVideo(
            eq(videoFile), 
            contains(clientId + "/" + videoFile.getOriginalFilename())
        );
        
        verify(sqsMessageService).sendProcessingMessage(messageCaptor.capture());
        
        Map<String, Object> capturedMessage = messageCaptor.getValue();
        assertEquals(clientId, capturedMessage.get("clientId"));
        assertEquals(videoFile.getOriginalFilename(), capturedMessage.get("filename"));
        assertEquals(videoFile.getContentType(), capturedMessage.get("contentType"));
        assertNotNull(capturedMessage.get("videoId"));
        assertNotNull(capturedMessage.get("s3Key"));
        assertNotNull(capturedMessage.get("timestamp"));
    }

    @Test
    void execute_WhenIOExceptionOccurs_ShouldThrowRuntimeException() throws IOException {
        // Arrange
        doThrow(new IOException("Test IO error")).when(s3FileStorageService)
            .uploadVideo(any(MultipartFile.class), anyString());
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> uploadVideoUseCase.execute(videoFile, clientId));
        verify(createVideoCoreApiClient).createVideo(any(), any(), any(), any());
        verify(s3FileStorageService).uploadVideo(any(), any());
        verifyNoInteractions(sqsMessageService);
    }
}