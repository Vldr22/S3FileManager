package org.resume.s3filemanager.service;

import com.amazonaws.services.kms.model.AWSKMSException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.properties.YandexStorageProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class YandexStorageService {

    private final AmazonS3 yandexS3Client;
    private final YandexStorageProperties properties;

    public void uploadFileYandexS3(MultipartFile file) {
        String uniqueFileName = generateUniqueUUIDFileName(file.getName());

        try(InputStream inputStream = new ByteArrayInputStream(file.getBytes())) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getBytes().length);
            metadata.setContentType(file.getContentType());

            PutObjectRequest request = new PutObjectRequest(properties.getBucketName(), uniqueFileName, inputStream, metadata);
            yandexS3Client.putObject(request);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new AWSKMSException(e.getMessage());
        }
        log.info("File uploaded: {} ", uniqueFileName);
    }

    private String generateUniqueUUIDFileName(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        extension = (extension != null && !extension.isBlank()) ? extension : "tmp";

        return UUID.randomUUID() + "." + extension;
    }

}
