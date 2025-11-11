package org.resume.s3filemanager.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.RequiredArgsConstructor;
import org.resume.s3filemanager.properties.YandexStorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({YandexStorageProperties.class})
public class YandexStorageConfig {

    private final YandexStorageProperties properties;

    @Bean
    public AmazonS3 yandexS3Client() {
        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                        properties.getAccessKey(),
                        properties.getSecretKey())))
                .withEndpointConfiguration(
                        new AmazonS3ClientBuilder.EndpointConfiguration(
                                properties.getEndpoint(),
                                properties.getRegion()
                        )
                )
                .build();
    }

}
