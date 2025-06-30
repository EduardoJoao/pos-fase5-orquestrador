package com.core.adapters.controller;

import com.core.config.JwtTokenUtil;
import com.core.domain.status.usecase.SearchAllVideosUseCase;
import lombok.RequiredArgsConstructor;
import org.openapitools.api.StatusApi;
import org.openapitools.model.StatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StatusController implements StatusApi {
    private final JwtTokenUtil jwtTokenUtil;
    private final SearchAllVideosUseCase searchAllVideosUseCase;

    @Override
    public ResponseEntity<StatusResponse> statusGet(){
         String email = jwtTokenUtil.getEmailFromToken();
        return  ResponseEntity.ok(searchAllVideosUseCase.execute(email));
    }
}
