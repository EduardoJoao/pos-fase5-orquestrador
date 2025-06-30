package com.core.domain.download.usecase;

import com.core.adapters.gateway.SearchIdVideoCoreApiClient;
import com.core.domain.core.model.Video;
import com.core.utils.S3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DownloadVideoUseCaseImpl implements DownloadVideoUseCase{

    private final SearchIdVideoCoreApiClient searchIdVideoCoreApiClient;
    private final S3Client s3Client;
    
    @Value("${s3.bucket.download}")
    private String bucketName;

    @Override
    public byte[] execute(String id) {
        // 1. Recuperar informações do vídeo através da API Core
        Video video = searchIdVideoCoreApiClient.validateVideo(id);
        if (video == null) {
            throw new RuntimeException("Vídeo não encontrado para o ID: " + id);
        }
        
        // 2. Construir o caminho do objeto no S3 (assumindo que o caminho esteja armazenado no objeto Video)
        String s3Key = S3Util.generateS3Key(video.getUserId(), video.getVideoZipKey());
        
        try {
            // 3. Fazer o download do arquivo do S3
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();
            
            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            
            // 4. Converter o InputStream para um array de bytes
            return toByteArray(s3Object);
            
        } catch (NoSuchKeyException e) {
            log.error("Arquivo não encontrado no S3: {}", s3Key, e);
            throw new RuntimeException("Arquivo não encontrado no S3: " + s3Key);
        } catch (Exception e) {
            log.error("Erro ao fazer download do arquivo do S3: {}", s3Key, e);
            throw new RuntimeException("Erro ao fazer download do arquivo: " + e.getMessage());
        }
    }
    
    private byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384]; // 16KB buffer
        
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        
        buffer.flush();
        return buffer.toByteArray();
    }
}