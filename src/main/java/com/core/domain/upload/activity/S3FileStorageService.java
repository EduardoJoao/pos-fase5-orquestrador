package com.core.domain.upload.activity;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileStorageService {

    private final S3Client s3Client;
    
    @Value("${s3.bucket}")
    private String s3BucketName;
    
    public void uploadVideo(MultipartFile video, String s3Key) throws IOException {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", video.getContentType());
        
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(s3BucketName)
            .key(s3Key)
            .contentType(video.getContentType())
            .metadata(metadata)
            .build();
        
        s3Client.putObject(
            putObjectRequest,
            RequestBody.fromInputStream(video.getInputStream(), video.getSize())
        );
        
        log.info("Video uploaded to S3: {}", s3Key);
    }

}