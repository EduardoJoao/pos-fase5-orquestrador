package com.core.adapters.gateway;

import com.core.domain.core.model.VideoRequset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateVideoCoreApiClientImpl implements CreateVideoCoreApiClient{

    private final RestTemplate restTemplate;
    
    @Value("${core.api.url}")
    private String coreApiUrl;

    @Override
    public void createVideo(String clientId, String filename, String uuid, String sizeVideo) {
        String endpoint = coreApiUrl + "/videos";
        
        log.info("Validating video with core API: clientId={}, filename={}", clientId, filename);
        
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                endpoint,
                getRequest(clientId, filename, uuid, sizeVideo),
                Void.class
            );
            
            response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error validating video with core API", e);
            throw new RuntimeException("Failed to validate video with core service: " + e.getMessage());
        }
    }

    private VideoRequset getRequest(String clientId, String filename, String uuid, String sizeVideo) {
        return VideoRequset.builder()
            .userId(clientId)
            .videoId(uuid)
            .videoKey(filename)
            .videoKeySize(sizeVideo)
            .build();
    }
}