package com.vtn.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

//    @Value("${aws.s3.region}")
//    private String region;

//    @Bean
//    @ConditionalOnProperty(name = "aws.s3.enabled", havingValue = "true")
//    public S3Client s3Client() {
//        AwsBasicCredentials credentials = AwsBasicCredentials.create()
//        return S3Client.builder()
//                .credentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
//                .region(Region.of(region))
//                .build();
//    }
}
