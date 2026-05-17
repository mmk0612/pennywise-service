package com.pennywise.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AWSConfig {

    @Value("${pennywise.s3.region:us-east-1}")
    private String s3Region;

    @Value("${pennywise.s3.access-key:}")
    private String s3AccessKey;

    @Value("${pennywise.s3.secret-key:}")
    private String s3SecretKey;

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder().region(Region.of(s3Region));

        if (s3AccessKey != null && !s3AccessKey.isBlank() && s3SecretKey != null && !s3SecretKey.isBlank()) {
            AwsBasicCredentials creds = AwsBasicCredentials.create(s3AccessKey, s3SecretKey);
            builder = builder.credentialsProvider(StaticCredentialsProvider.create(creds));
        } else {
            builder = builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }
}
