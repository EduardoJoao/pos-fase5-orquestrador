package com.core.adapters.controller;

import com.core.config.JwtTokenUtil;
import com.core.domain.upload.usecase.UploadVideoUseCase;
import lombok.RequiredArgsConstructor;
import org.openapitools.api.UploadApi;
import org.openapitools.model.UploadPost200Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class UploadController implements UploadApi {

    private final UploadVideoUseCase uploadVideoUseCase;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public ResponseEntity<UploadPost200Response> uploadPost(MultipartFile video){
        String email = jwtTokenUtil.getEmailFromToken();
        uploadVideoUseCase.execute(video, email);
        return ResponseEntity.ok(null);
    }
}
