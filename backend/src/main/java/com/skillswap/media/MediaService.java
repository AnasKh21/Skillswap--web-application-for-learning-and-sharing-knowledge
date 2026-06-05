package com.skillswap.media;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class MediaService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_BYTES = 5 * 1024 * 1024; // 5 MB

    @Value("${app.media.upload-dir:./uploads}")
    private String uploadDir;

    public String storeAvatar(UUID userId, MultipartFile file) {
        return store(userId, file, "avatars");
    }

    public String storeBanner(UUID userId, MultipartFile file) {
        return store(userId, file, "banners");
    }

    private String store(UUID userId, MultipartFile file, String subfolder) {
        validate(file);
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().resolve(subfolder);
            Files.createDirectories(dir);

            String ext = extension(file.getOriginalFilename());
            String filename = userId + "." + ext;
            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + subfolder + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file", e);
        }
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
