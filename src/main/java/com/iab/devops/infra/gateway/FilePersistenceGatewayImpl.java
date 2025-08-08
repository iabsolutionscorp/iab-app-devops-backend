package com.iab.devops.infra.gateway;


import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.iab.devops.application.gateway.FilePersistenceGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@RequiredArgsConstructor
@Repository
public class FilePersistenceGatewayImpl implements FilePersistenceGateway {

    @Value("${aws.s3.bucket}")
    private String bucketName;
    private final AmazonS3 amazonS3;


    @Override
    public void upload(InputStream inputStream, String location, String contentType) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);

            amazonS3.putObject(new PutObjectRequest(bucketName, location, inputStream, metadata));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer upload do arquivo para o S3. Motivo: " + e.getMessage());
        }
    }

    @Override
    public InputStream download(String location) {
        try {
            S3Object s3Object = amazonS3.getObject(bucketName, location);
            return s3Object.getObjectContent();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao fazer download do arquivo do S3. Motivo: " + e.getMessage(), e);
        }
    }

    @Override
    public URL getUrl(String location) {
        try {
            Date expiration = new Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += 1000 * 60 * 5; // 5 minutos
            expiration.setTime(expTimeMillis);

            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucketName, location)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);

            return amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar a URL assinada para o S3. Motivo: " + e.getMessage());
        }
    }

    @Override
    public void delete(String location) {
        try {
            amazonS3.deleteObject(bucketName, location);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao deletar o arquivo do S3. Motivo: " + e.getMessage());
        }
    }
}

