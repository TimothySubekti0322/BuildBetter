package com.buildbetter.shared.util;

import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.buildbetter.shared.exception.InternalServerErrorException;
import com.buildbetter.shared.model.S3Properties;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Properties s3Properties;

    public String uploadFile(MultipartFile file, String folder) {
        String uniqueFileName = folder + UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            S3Client s3 = createS3Client();
            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(s3Properties.getBucketName())
                            .key(uniqueFileName)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return s3Properties.getBaseUrl() + "/" + uniqueFileName;

        } catch (IOException e) {
            throw new InternalServerErrorException("Failed to upload file to S3: " + e.getMessage());
        }
    }

    public void deleteFile(String key) {
        String extractedKey = extractKey(key);
        try {
            S3Client s3 = createS3Client();
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Properties.getBucketName())
                    .key(extractedKey)
                    .build());
        } catch (S3Exception e) {
            throw new InternalServerErrorException(
                    "Failed to delete file from S3: " + e.awsErrorDetails().errorMessage());
        }
    }

    private S3Client createS3Client() {
        return S3Client.builder()
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        s3Properties.getAccessKey(),
                                        s3Properties.getSecretKey())))
                .build();
    }

    private String extractKey(String originalKey) {
        if (originalKey.startsWith(s3Properties.getBaseUrl())) {
            return originalKey.substring(s3Properties.getBaseUrl().length() + 1);
        }
        return originalKey;
    }
}