package com.core.domain.upload.usecase;

import org.openapitools.model.UploadPost200Response;
import org.springframework.web.multipart.MultipartFile;

public interface UploadVideoUseCase {

    UploadPost200Response execute(MultipartFile video, String clientId);
}
