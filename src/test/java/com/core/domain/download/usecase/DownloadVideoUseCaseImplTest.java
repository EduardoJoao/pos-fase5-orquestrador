package com.core.domain.download.usecase;

import com.core.adapters.gateway.SearchIdVideoCoreApiClient;
import com.core.domain.core.model.Video;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DownloadVideoUseCaseImplTest {

    @Mock
    private SearchIdVideoCoreApiClient searchIdVideoCoreApiClient;

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private DownloadVideoUseCaseImpl downloadVideoUseCaseImpl;

    private final String videoId = "video-123";
    private final String userId = "user-456";
    private final String videoZipKey = "test-video.zip";
    private final byte[] testFileContent = "test file content".getBytes();
    private Video mockVideo;

    @BeforeEach
    void setUp() {
        try {
            var field = downloadVideoUseCaseImpl.getClass().getDeclaredField("bucketName");
            field.setAccessible(true);
            field.set(downloadVideoUseCaseImpl, "test-bucket");
        } catch (Exception e) {
            fail("Failed to set bucketName field");
        }
        
        mockVideo = new Video();
        mockVideo.setId(videoId);
        mockVideo.setUserId(userId);
        mockVideo.setVideoZipKey(videoZipKey);
        mockVideo.setCreatedAt(LocalDateTime.now());
        mockVideo.setStatus("COMPLETED");
    }

    @Test
    void execute_ShouldDownloadFileSuccessfully() throws IOException {
        when(searchIdVideoCoreApiClient.validateVideo(videoId)).thenReturn(mockVideo);
        
        ResponseInputStream<GetObjectResponse> mockResponse = mock(ResponseInputStream.class);
        when(mockResponse.read(any(byte[].class), anyInt(), anyInt()))
            .thenAnswer(invocation -> {
                byte[] buffer = invocation.getArgument(0);
                System.arraycopy(testFileContent, 0, buffer, 0, testFileContent.length);
                return testFileContent.length;
            })
            .thenReturn(-1);
        
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockResponse);
        
        byte[] result = downloadVideoUseCaseImpl.execute(videoId);
        
        assertNotNull(result);
        verify(searchIdVideoCoreApiClient).validateVideo(videoId);
        verify(s3Client).getObject(any(GetObjectRequest.class));
    }
    
    @Test
    void execute_WhenVideoNotFound_ShouldThrowException() {
        when(searchIdVideoCoreApiClient.validateVideo(videoId)).thenReturn(null);
        
        Exception exception = assertThrows(RuntimeException.class, () ->
            downloadVideoUseCaseImpl.execute(videoId)
        );
        assertTrue(exception.getMessage().contains("não encontrado"));
        verify(searchIdVideoCoreApiClient).validateVideo(videoId);
        verifyNoInteractions(s3Client);
    }
    
    @Test
    void execute_WhenS3KeyNotFound_ShouldThrowException() {
        when(searchIdVideoCoreApiClient.validateVideo(videoId)).thenReturn(mockVideo);
        when(s3Client.getObject(any(GetObjectRequest.class)))
            .thenThrow(NoSuchKeyException.builder().build());
        
        Exception exception = assertThrows(RuntimeException.class, () ->
            downloadVideoUseCaseImpl.execute(videoId)
        );
        assertTrue(exception.getMessage().contains("não encontrado no S3"));
        verify(searchIdVideoCoreApiClient).validateVideo(videoId);
        verify(s3Client).getObject(any(GetObjectRequest.class));
    }
}