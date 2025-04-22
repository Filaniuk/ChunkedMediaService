package com.mfilaniu.trustedmediaservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

@Service
public class AWSService {

    private static final Logger LOG = Logger.getLogger(AWSService.class.getName());

    @Value("${aws.bucket-name}")
    private String bucketName;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public AWSService(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    public byte[] downloadEncrypted(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> objectStream = s3Client.getObject(getObjectRequest)) {
            return objectStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to download encrypted file from S3", e);
        }
    }

    public InputStream downloadEncryptedStream(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return s3Client.getObject(request);
    }

    public byte[] download(String s3Key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        try (ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(request)) {
            return s3Stream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to download chunk from S3: " + s3Key, e);
        }
    }

}

