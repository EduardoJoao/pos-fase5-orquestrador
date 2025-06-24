package com.core.adapters.gateway;

import com.core.domain.core.model.Video;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchAllVideosCoreApiClient {

    private final RestTemplate restTemplate;
    
    @Value("${core.api.url}")
    private String coreApiUrl;

    public List<Video> getAllVideos(String email) {
        String endpoint = coreApiUrl + "/videos";

        try {
            ResponseEntity<List<Video>> response = restTemplate.exchange(
                endpoint,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Video>>() {}
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error retrieving videos from core API", e);
            throw new RuntimeException("Failed to retrieve videos from core service: " + e.getMessage());
        }
    }
}