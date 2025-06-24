package com.core.domain.upload.activity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SQSMessageServiceTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SQSMessageService sqsMessageService;

    private Map<String, Object> messagePayload;
    private final String jsonPayload = "{\"key\":\"value\",\"videoId\":\"123\"}";

    @BeforeEach
    void setUp() {
        messagePayload = new HashMap<>();
        messagePayload.put("key", "value");
        messagePayload.put("videoId", "123");
        
        // Configurando variável de ambiente para teste
        try {
            var field = sqsMessageService.getClass().getDeclaredField("processQueueUrl");
            field.setAccessible(true);
            field.set(sqsMessageService, "test-queue-url");
        } catch (Exception e) {
            fail("Failed to set processQueueUrl field");
        }
    }

    @Test
    void sendProcessingMessage_ShouldSendMessageToSQS() throws JsonProcessingException {
        // Arrange
        when(objectMapper.writeValueAsString(messagePayload)).thenReturn(jsonPayload);
        
        // Act
        sqsMessageService.sendProcessingMessage(messagePayload);
        
        // Assert
        verify(objectMapper).writeValueAsString(messagePayload);
        
        var requestCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(requestCaptor.capture());
        
        SendMessageRequest capturedRequest = requestCaptor.getValue();
        assertEquals("test-queue-url", capturedRequest.queueUrl());
        assertEquals(jsonPayload, capturedRequest.messageBody());
    }
    
    @Test
    void sendProcessingMessage_WhenSerializationFails_ShouldThrowRuntimeException() throws JsonProcessingException {
        // Arrange
        when(objectMapper.writeValueAsString(messagePayload))
            .thenThrow(new JsonProcessingException("Serialization error") {});
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> 
            sqsMessageService.sendProcessingMessage(messagePayload)
        );
        
        assertTrue(exception.getMessage().contains("Failed to send message"));
        verify(objectMapper).writeValueAsString(messagePayload);
        verifyNoInteractions(sqsClient);
    }
}