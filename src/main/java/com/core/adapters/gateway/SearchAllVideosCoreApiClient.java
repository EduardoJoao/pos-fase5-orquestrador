package com.core.adapters.gateway;

import com.core.domain.core.model.Video;

import java.util.List;

public interface SearchAllVideosCoreApiClient {

    List<Video> getAllVideos(String email);
}
