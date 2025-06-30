package com.core.adapters.gateway;

import com.core.domain.core.model.Video;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchIdVideoCoreApiClientImpl implements SearchIdVideoCoreApiClient{

    private final RestTemplate restTemplate;

    @Value("${core.api.url}")
    private String coreApiUrl;

    @Override
    public Video validateVideo(String id) {
        String endpoint = coreApiUrl + "/videos/" + id;

        try {
            ResponseEntity<Video> response = restTemplate.getForEntity(
                endpoint,
                Video.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error validating video with core API", e);
            throw new RuntimeException("Failed to validate video with core service: " + e.getMessage());
        }
    }
}