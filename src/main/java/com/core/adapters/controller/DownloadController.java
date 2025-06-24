package com.core.adapters.controller;

import com.core.config.JwtTokenUtil;
import com.core.domain.download.usecase.DownloadVideoUseCase;
import lombok.RequiredArgsConstructor;
import org.openapitools.api.DownloadApi;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DownloadController implements DownloadApi {

    private final JwtTokenUtil jwtTokenUtil;
    private final DownloadVideoUseCase downloadVideoUseCase;
    
    @Override
    public ResponseEntity<Resource> downloadIdPost(String id){
        String email = jwtTokenUtil.getEmailFromToken();
        byte[] fileContent = downloadVideoUseCase.execute(id);

        ByteArrayResource resource = new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return "frames-" + id + ".zip";
            }
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "frames-" + id + ".zip");

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}
