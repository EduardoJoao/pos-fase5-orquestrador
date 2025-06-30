package com.core.domain.status.usecase;

import com.core.adapters.gateway.SearchAllVideosCoreApiClient;
import com.core.domain.core.model.Video;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.FileInfo;
import org.openapitools.model.StatusResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchAllVideosUseCaseImpl implements SearchAllVideosUseCase{

    private final SearchAllVideosCoreApiClient searchAllVideosCoreApiClient;

    @Override
    public StatusResponse execute(String email) {
        try {
            log.info("Buscando todos os vídeos do cliente");
            List<Video> videos = searchAllVideosCoreApiClient.getAllVideos(email);
            return getResponse(videos);
        } catch (Exception e) {
            log.error("Erro ao buscar vídeos do cliente", e);
            return null;
        }
    }

    private StatusResponse getResponse(List<Video> videos){
        List<FileInfo> itensList =  videos.stream()
            .map(video -> FileInfo.builder()
                .id(video.getId())
                .status(video.getStatus())
                .createdAt(video.getCreatedAt().toString())
                .filename(video.getVideoZipKey())
                .size(video.getVideoZipKeySize())
                .build())
            .toList();
        var total = itensList.size();
        return StatusResponse.builder().files(itensList).total(total).build();
    }
}