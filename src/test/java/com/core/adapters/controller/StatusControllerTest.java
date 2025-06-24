package com.core.adapters.controller;

import com.core.config.JwtTokenUtil;
import com.core.domain.status.usecase.SearchAllVideosUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.FileInfo;
import org.openapitools.model.StatusResponse;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatusControllerTest {

    @Mock
    private SearchAllVideosUseCase searchAllVideosUseCase;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @InjectMocks
    private StatusController statusController;

    private final String userEmail = "user@example.com";
    private StatusResponse mockResponse;

    @BeforeEach
    void setUp() {
        List<FileInfo> files = Arrays.asList(
            FileInfo.builder().id("1").filename("video1.zip").build(),
            FileInfo.builder().id("2").filename("video2.zip").build()
        );
        mockResponse = StatusResponse.builder()
            .total(2)
            .files(files)
            .build();

        when(jwtTokenUtil.getEmailFromToken()).thenReturn(userEmail);
        when(searchAllVideosUseCase.execute(userEmail)).thenReturn(mockResponse);
    }

    @Test
    void statusGet_ShouldReturnStatusResponseFromUseCase() {
        // Act
        ResponseEntity<StatusResponse> response = statusController.statusGet();
        
        // Assert
        assertNotNull(response);
        assertEquals(mockResponse, response.getBody());
        assertEquals(2, response.getBody().getTotal());
        assertEquals(2, response.getBody().getFiles().size());
        
        verify(jwtTokenUtil, times(1)).getEmailFromToken();
        verify(searchAllVideosUseCase, times(1)).execute(userEmail);
    }
}