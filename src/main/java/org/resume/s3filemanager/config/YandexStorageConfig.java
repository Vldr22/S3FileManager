package org.resume.s3filemanager.config;


import lombok.RequiredArgsConstructor;
import org.resume.s3filemanager.properties.YandexStorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({YandexStorageProperties.class})
public class YandexStorageConfig {

    private final YandexStorageProperties properties;

    @Bean
    public S3Client yandexS3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                properties.getAccessKey(),
                properties.getSecretKey()
        );

        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(properties.getEndpoint()))
                .region(Region.of(properties.getRegion()))
                .build();
    }

}
