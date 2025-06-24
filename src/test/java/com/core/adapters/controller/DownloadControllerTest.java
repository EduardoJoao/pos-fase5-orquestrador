package com.core.adapters.controller;

import com.core.config.JwtTokenUtil;
import com.core.domain.download.usecase.DownloadVideoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DownloadControllerTest {

    @Mock
    private DownloadVideoUseCase downloadVideoUseCase;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @InjectMocks
    private DownloadController downloadController;

    private final String videoId = "video-123";
    private final String userEmail = "user@example.com";
    private final byte[] fileContent = "mock file content".getBytes();

    @BeforeEach
    void setUp() {
        when(jwtTokenUtil.getEmailFromToken()).thenReturn(userEmail);
        when(downloadVideoUseCase.execute(videoId)).thenReturn(fileContent);
    }

    @Test
    void downloadIdPost_ShouldReturnResourceWithCorrectHeaders() {
        // Act
        ResponseEntity<Resource> response = downloadController.downloadIdPost(videoId);
        
        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getContentDisposition().toString().contains(videoId));
        
        verify(jwtTokenUtil, times(1)).getEmailFromToken();
        verify(downloadVideoUseCase, times(1)).execute(videoId);
    }
}