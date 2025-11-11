package org.resume.s3filemanager.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.exception.Messages;
import org.resume.s3filemanager.exception.S3YandexException;
import org.resume.s3filemanager.properties.YandexStorageProperties;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class YandexStorageService {

    private final AmazonS3 yandexS3Client;
    private final YandexStorageProperties properties;

    public void uploadFileYandexS3(String uniqueFileName, byte[] bytes, String contentType) {
        ObjectMetadata metadata = createObjectMetadata(bytes, contentType);

        try (InputStream is = new ByteArrayInputStream(bytes)) {
            PutObjectRequest request = new PutObjectRequest(
                    properties.getBucketName(),
                    uniqueFileName,
                    is,
                    metadata
            );
            yandexS3Client.putObject(request);
            log.info("File uploaded to S3: {}, size: {} bytes", uniqueFileName, bytes.length);

        } catch (AmazonS3Exception e) {
            log.error("S3 error uploading file: {}", uniqueFileName, e);
            throw new S3YandexException(Messages.FILE_STORAGE_ERROR, e);
        } catch (Exception e) {
            log.error("Unexpected error uploading file: {}", uniqueFileName, e);
            throw new S3YandexException(Messages.FILE_STORAGE_ERROR, e);
        }
    }

    public byte[] downloadFileYandexS3(String uniqueFileName) {
        try (S3Object s3Object = yandexS3Client.getObject(properties.getBucketName(), uniqueFileName);
             S3ObjectInputStream inputStream = s3Object.getObjectContent()) {

            byte[] data = IOUtils.toByteArray(inputStream);
            log.info("File downloaded from S3: {}, size: {} bytes", uniqueFileName, data.length);
            return data;

        } catch (AmazonS3Exception e) {
            log.error("File not found in S3: {}", uniqueFileName, e);
            throw new S3YandexException(Messages.FILE_STORAGE_ERROR, e);
        } catch (IOException e) {
            log.error("IO error downloading file: {}", uniqueFileName, e);
            throw new S3YandexException(Messages.FILE_STORAGE_ERROR, e);
        }
    }

    public void deleteFileYandexS3(String fileName) {
        try {
            yandexS3Client.deleteObject(properties.getBucketName(), fileName);
            log.info("File deleted from S3: {}", fileName);
        } catch (AmazonS3Exception e) {
            log.error("S3 error deleting file: {}", fileName, e);
            throw new S3YandexException(Messages.FILE_STORAGE_ERROR, e);
        }
    }

    private ObjectMetadata createObjectMetadata(byte[] bytes, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        metadata.setContentType(contentType);
        return metadata;
    }
}
