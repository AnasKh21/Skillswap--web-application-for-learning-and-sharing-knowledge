package com.skillswap.media;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
public class MediaService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_BYTES = 5 * 1024 * 1024;

    @Value("${app.media.s3.bucket-name}")
    private String bucket;

    @Value("${app.media.s3.region}")
    private String region;

    private final S3Client s3;
    private final S3Presigner presigner;

    public MediaService(S3Client s3, S3Presigner presigner) {
        this.s3 = s3;
        this.presigner = presigner;
    }

    public String storeAvatar(UUID userId, MultipartFile file) {
        return store(userId, file, "avatars");
    }

    public String storeBanner(UUID userId, MultipartFile file) {
        return store(userId, file, "banners");
    }

    private String store(UUID userId, MultipartFile file, String folder) {
        validate(file);
        try {
            String key = folder + "/" + userId + "." + extension(file.getOriginalFilename());

            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
        } catch (IOException e) {
            throw new RuntimeException("Could not upload file to S3", e);
        }
    }

    public String presignUrl(String storedUrl) {
        String key = URI.create(storedUrl).getPath().substring(1);
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(r -> r.bucket(bucket).key(key))
                .build();
        return presigner.presignGetObject(presignRequest).url().toString();
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty())
            throw new IllegalArgumentException("File is empty");
        if (file.getSize() > MAX_BYTES)
            throw new IllegalArgumentException("File too large — max 5 MB");
        if (!ALLOWED_TYPES.contains(file.getContentType()))
            throw new IllegalArgumentException("Only JPEG, PNG, WebP and GIF are allowed");
    }

    private String extension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
