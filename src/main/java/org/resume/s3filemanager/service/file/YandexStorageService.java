package org.resume.s3filemanager.service.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        } catch (AmazonS3Exception e) {
            log.error("S3 error uploading file: {}", uniqueFileName, e);
            throw new S3YandexException(e, uniqueFileName);
        } catch (Exception e) {
            log.error("Unexpected error uploading file: {}", uniqueFileName, e);
            throw new S3YandexException(e, uniqueFileName);
        }
    }

    public byte[] downloadFileYandexS3(String uniqueFileName) {
        try (S3Object s3Object = yandexS3Client.getObject(properties.getBucketName(), uniqueFileName);
             S3ObjectInputStream inputStream = s3Object.getObjectContent()) {

            return IOUtils.toByteArray(inputStream);

        } catch (AmazonS3Exception e) {
            log.error("File not found in S3: {}", uniqueFileName, e);
            throw new S3YandexException(e, uniqueFileName);
        } catch (IOException e) {
            log.error("IO error downloading file: {}", uniqueFileName, e);
            throw new S3YandexException(e, uniqueFileName);
        }
    }

    public void deleteFileYandexS3(String fileName) {
        try {
            yandexS3Client.deleteObject(properties.getBucketName(), fileName);
        } catch (AmazonS3Exception e) {
            log.error("S3 error deleting file: {}", fileName, e);
            throw new S3YandexException(e, fileName);
        }
    }

    private ObjectMetadata createObjectMetadata(byte[] bytes, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        metadata.setContentType(contentType);
        return metadata;
    }
}
