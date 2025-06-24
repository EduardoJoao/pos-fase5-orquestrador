package com.core.adapters.controller;

import com.core.config.JwtTokenUtil;
import com.core.domain.upload.usecase.UploadVideoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.UploadPost200Response;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UploadControllerTest {

    @Mock
    private UploadVideoUseCase uploadVideoUseCase;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @InjectMocks
    private UploadController uploadController;

    private MultipartFile videoFile;
    private final String userEmail = "user@example.com";

    @BeforeEach
    void setUp() {
        videoFile = new MockMultipartFile(
                "video",
                "test-video.mp4",
                "video/mp4",
                "test video content".getBytes()
        );

        when(jwtTokenUtil.getEmailFromToken()).thenReturn(userEmail);
    }

    @Test
    void uploadPost_ShouldCallUseCaseWithCorrectParameters() {
        // Act
        ResponseEntity<UploadPost200Response> response = uploadController.uploadPost(videoFile);
        
        // Assert
        verify(jwtTokenUtil, times(1)).getEmailFromToken();
        verify(uploadVideoUseCase, times(1)).execute(videoFile, userEmail);
        assertNull(response.getBody());
    }
}