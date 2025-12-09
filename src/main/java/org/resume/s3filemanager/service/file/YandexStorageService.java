package org.resume.s3filemanager.service.file;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.exception.S3YandexException;
import org.resume.s3filemanager.properties.YandexStorageProperties;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class YandexStorageService {

    private final S3Client yandexS3Client;
    private final YandexStorageProperties properties;

    public void uploadFileYandexS3(String uniqueFileName, byte[] bytes, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.getBucketName())
                    .key(uniqueFileName)
                    .contentType(contentType)
                    .contentLength((long) bytes.length)
                    .build();

            yandexS3Client.putObject(request, RequestBody.fromBytes(bytes));

        } catch (S3Exception e) {
            log.error("S3 error uploading file: {}", uniqueFileName, e);
            throw new S3YandexException(e, uniqueFileName);
        }
    }

    public byte[] downloadFileYandexS3(String uniqueFileName) {
        try (ResponseInputStream<GetObjectResponse> response = yandexS3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(properties.getBucketName())
                        .key(uniqueFileName)
                        .build()
        )) {
            return response.readAllBytes();

        } catch (S3Exception e) {
            log.error("S3 error downloading file: {}", uniqueFileName, e);
            throw new S3YandexException(e, uniqueFileName);
        } catch (IOException e) {
            log.error("IO error downloading file: {}", uniqueFileName, e);
            throw new S3YandexException(e, uniqueFileName);
        }
    }

    public void deleteFileYandexS3(String uniqueFileName) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(properties.getBucketName())
                    .key(uniqueFileName)
                    .build();

            yandexS3Client.deleteObject(request);

        } catch (S3Exception e) {
            log.error("S3 error deleting file: {}", uniqueFileName, e);
            throw new S3YandexException(e, uniqueFileName);
        }
    }
}
