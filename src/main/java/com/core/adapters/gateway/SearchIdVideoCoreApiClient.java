package com.core.adapters.gateway;

import com.core.domain.core.model.Video;

public interface SearchIdVideoCoreApiClient {

    Video validateVideo(String id);
}
