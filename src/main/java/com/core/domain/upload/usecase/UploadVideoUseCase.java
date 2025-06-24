package com.core.domain.upload.usecase;

import com.core.adapters.gateway.CreateVideoCoreApiClient;
import com.core.domain.upload.activity.S3FileStorageService;
import com.core.domain.upload.activity.SQSMessageService;
import com.core.utils.S3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.UploadPost200Response;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadVideoUseCase {
    
    private final CreateVideoCoreApiClient createVideoCoreApiClient;
    private final S3FileStorageService s3FileStorageService;
    private final SQSMessageService sqsMessageService;

    public UploadPost200Response execute(MultipartFile video, String clientId) {
        log.info("Processing video upload for client: {}, file: {}", clientId, video.getOriginalFilename());
        String s3Key = S3Util.generateS3Key(clientId, video.getOriginalFilename());
        String uuid = java.util.UUID.randomUUID().toString();
        try {
            createVideoCoreApiClient.createVideo(clientId, video.getOriginalFilename(), uuid, getVideoSizeInMB(video));
            
            // Upload the video to S3
            s3FileStorageService.uploadVideo(video, s3Key);

            // Send processing message to SQS
            Map<String, Object> messagePayload = new HashMap<>();
            messagePayload.put("s3Key", s3Key);
            messagePayload.put("videoId", uuid);
            messagePayload.put("clientId", clientId);
            messagePayload.put("filename", video.getOriginalFilename());
            messagePayload.put("contentType", video.getContentType());
            messagePayload.put("timestamp", System.currentTimeMillis());
            
            sqsMessageService.sendProcessingMessage(messagePayload);
            
            log.info("Video uploaded and queued for processing. S3 key: {}", s3Key);

            return new UploadPost200Response(
                "Video upload successful. Processing started."
            );
        } catch (IOException e) {
            log.error("Failed to process video upload", e);
            throw new RuntimeException("Failed to process video upload: " + e.getMessage());
        }
    }

    private String getVideoSizeInMB(MultipartFile video) {
        double sizeInMB = (double) video.getSize() / (1024 * 1024);
        return String.format("%.2f MB", sizeInMB);
    }
}