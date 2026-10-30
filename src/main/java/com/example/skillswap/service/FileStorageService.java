package com.example.skillswap.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    public static final long MAX_PROFILE_PHOTO_BYTES = 5L * 1024 * 1024;
    private static final String PUBLIC_PREFIX = "/uploads/";

    private final Path uploadRoot;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void createUploadDirectory() throws IOException {
        Files.createDirectories(uploadRoot);
    }

    public String storeProfilePhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please select an image to upload");
        }

        if (file.getSize() > MAX_PROFILE_PHOTO_BYTES) {
            throw new RuntimeException("Profile photos must be 5 MB or smaller");
        }

        String extension = detectImageExtension(file);

        if (extension == null) {
            throw new RuntimeException("Only JPEG, PNG, and WebP images are supported");
        }

        String filename = UUID.randomUUID() + extension;
        Path target = uploadRoot.resolve(filename).normalize();

        if (!target.startsWith(uploadRoot)) {
            throw new RuntimeException("Invalid upload path");
        }

        try (InputStream input = file.getInputStream()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("Could not store the profile photo", ex);
        }

        return PUBLIC_PREFIX + filename;
    }

    public void deleteManagedPhoto(String photoPath) {
        if (photoPath == null || !photoPath.startsWith(PUBLIC_PREFIX)) {
            return;
        }

        String filename = Paths.get(photoPath).getFileName().toString();
        Path target = uploadRoot.resolve(filename).normalize();

        if (!target.startsWith(uploadRoot)) {
            return;
        }

        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // A stale file should not prevent the profile update from succeeding.
        }
    }

    private String detectImageExtension(MultipartFile file) {
        byte[] header = new byte[12];

        try (InputStream input = file.getInputStream()) {
            int read = input.read(header);

            if (read < 4) {
                return null;
            }

            if ((header[0] & 0xFF) == 0xFF
                    && (header[1] & 0xFF) == 0xD8
                    && (header[2] & 0xFF) == 0xFF) {
                return ".jpg";
            }

            if ((header[0] & 0xFF) == 0x89
                    && header[1] == 0x50
                    && header[2] == 0x4E
                    && header[3] == 0x47) {
                return ".png";
            }

            if (read >= 12
                    && header[0] == 'R'
                    && header[1] == 'I'
                    && header[2] == 'F'
                    && header[3] == 'F'
                    && header[8] == 'W'
                    && header[9] == 'E'
                    && header[10] == 'B'
                    && header[11] == 'P') {
                return ".webp";
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not read the selected image", ex);
        }

        return null;
    }
}
