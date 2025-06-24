package com.core.domain.upload.activity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class S3FileStorageServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3FileStorageService s3FileStorageService;

    private MultipartFile videoFile;
    private final String s3Key = "user@example.com/test-video.mp4";

    @BeforeEach
    void setUp() {
        videoFile = new MockMultipartFile(
                "video",
                "test-video.mp4",
                "video/mp4",
                "test video content".getBytes()
        );
        
        // Configurando variável de ambiente para teste
        try {
            var field = s3FileStorageService.getClass().getDeclaredField("s3BucketName");
            field.setAccessible(true);
            field.set(s3FileStorageService, "test-bucket");
        } catch (Exception e) {
            fail("Failed to set s3BucketName field");
        }
    }

    @Test
    void uploadVideo_ShouldUploadVideoToS3Successfully() throws IOException {
        // Act
        s3FileStorageService.uploadVideo(videoFile, s3Key);
        
        // Assert
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
    
    @Test
    void uploadVideo_ShouldSetCorrectMetadata() throws IOException {
        // Arrange
        var putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        
        // Act
        s3FileStorageService.uploadVideo(videoFile, s3Key);
        
        // Assert
        verify(s3Client).putObject(putObjectRequestCaptor.capture(), any(RequestBody.class));
        
        PutObjectRequest capturedRequest = putObjectRequestCaptor.getValue();
        assertEquals("test-bucket", capturedRequest.bucket());
        assertEquals(s3Key, capturedRequest.key());
        assertEquals(videoFile.getContentType(), capturedRequest.contentType());
        assertTrue(capturedRequest.metadata().containsKey("Content-Type"));
        assertEquals(videoFile.getContentType(), capturedRequest.metadata().get("Content-Type"));
    }
    
    @Test
    void uploadVideo_WhenS3ClientThrowsException_ShouldPropagateException() throws IOException {
        // Arrange
        doThrow(new RuntimeException("S3 Error")).when(s3Client)
            .putObject(any(PutObjectRequest.class), any(RequestBody.class));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            s3FileStorageService.uploadVideo(videoFile, s3Key)
        );
        
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}