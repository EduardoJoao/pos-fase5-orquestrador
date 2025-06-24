package com.core.domain.upload.activity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SQSMessageService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${sqs.process}")
    private String processQueueUrl;

    public void sendProcessingMessage(Map<String, Object> messageBody) {
        try {
            String messageJson = objectMapper.writeValueAsString(messageBody);
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(processQueueUrl)
                    .messageBody(messageJson)
                    .build();
            
            sqsClient.sendMessage(sendMessageRequest);
            log.info("Message sent to processing queue: {}", messageJson);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message body", e);
            throw new RuntimeException("Failed to send message to SQS", e);
        }
    }
}