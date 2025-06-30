package com.core.domain.status.usecase;

import org.openapitools.model.StatusResponse;

public interface SearchAllVideosUseCase {

    StatusResponse execute(String email);
}
