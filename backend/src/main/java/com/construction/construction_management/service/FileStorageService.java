package com.construction.construction_management.service;

import com.construction.construction_management.exception.BadRequestException;
import com.construction.construction_management.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

/**
 * Stores site photos on a local volume. Only JPEG, PNG and WebP files are accepted, the
 * extension is chosen from the detected bytes (never from the client filename), and every
 * stored name is a random UUID so a URL cannot be guessed or used to traverse the volume.
 */
@Service
public class FileStorageService {

    private static final String JPEG = "image/jpeg";
    private static final String PNG = "image/png";
    private static final String WEBP = "image/webp";
    private static final String FILENAME_PATTERN =
            "^project-\\d+-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|webp)$";
    /** Must match {@link com.construction.construction_management.controller.FileController}. */
    private static final String PUBLIC_PATH = "/api/files";

    private final Path root;
    private final long maxBytes;

    public FileStorageService(@Value("${app.uploads.dir:./uploads}") String uploadDir,
                              @Value("${app.uploads.max-bytes:10485760}") long maxBytes) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
        try {
            Files.createDirectories(root);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not create upload directory " + root, ex);
        }
    }

    public StoredFile store(MultipartFile file, int projectId) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Choose a photo to upload");
        }
        if (file.getSize() > maxBytes) {
            throw new BadRequestException("Photo is too large. Maximum size is " + maxMegabytes() + " MB");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new BadRequestException("The upload could not be read. Please try again");
        }
        if (bytes.length > maxBytes) {
            throw new BadRequestException("Photo is too large. Maximum size is " + maxMegabytes() + " MB");
        }

        String detectedType = detectType(bytes);
        if (detectedType == null) {
            throw new BadRequestException("Only JPG, PNG or WebP images are supported");
        }

        String declaredType = normalizeContentType(file.getContentType());
        if (declaredType != null && !declaredType.equals(detectedType)) {
            throw new BadRequestException("The file type does not match the image content");
        }

        String extension = extensionFor(detectedType);
        String filename = "project-" + projectId + "-" + UUID.randomUUID() + "." + extension;
        Path target = root.resolve(filename).normalize();
        if (!target.startsWith(root)) {
            throw new BadRequestException("Invalid filename");
        }

        Path temp = null;
        try {
            temp = Files.createTempFile(root, "upload-", ".tmp");
            Files.write(temp, bytes);
            try {
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
            temp = null;
        } catch (IOException ex) {
            throw new IllegalStateException("Could not store the uploaded photo", ex);
        } finally {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ignored) {
                    // A leftover temp file is harmless; it is never served.
                }
            }
        }

        return new StoredFile(filename, PUBLIC_PATH + "/" + filename, detectedType, bytes.length);
    }

    public Resource load(String filename) {
        if (filename == null || !filename.matches(FILENAME_PATTERN)) {
            throw new ResourceNotFoundException("File not found");
        }
        Path target = root.resolve(filename).normalize();
        if (!target.startsWith(root) || !Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) {
            throw new ResourceNotFoundException("File not found");
        }
        return new FileSystemResource(target);
    }

    public String contentType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return switch (extension) {
            case "jpg", "jpeg" -> JPEG;
            case "png" -> PNG;
            case "webp" -> WEBP;
            default -> "application/octet-stream";
        };
    }

    private long maxMegabytes() {
        return Math.max(1, maxBytes / (1024 * 1024));
    }

    private static String normalizeContentType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.toLowerCase(Locale.ROOT).trim();
        if ("image/jpg".equals(normalized) || "image/pjpeg".equals(normalized)) {
            return JPEG;
        }
        // Browsers and mobile clients sometimes send this for a valid image.
        if ("application/octet-stream".equals(normalized)) {
            return null;
        }
        return normalized;
    }

    private static String extensionFor(String contentType) {
        return switch (contentType) {
            case JPEG -> "jpg";
            case PNG -> "png";
            case WEBP -> "webp";
            default -> throw new BadRequestException("Unsupported image type");
        };
    }

    private static String detectType(byte[] bytes) {
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF) {
            return JPEG;
        }
        if (bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G'
                && (bytes[4] & 0xFF) == 0x0D && (bytes[5] & 0xFF) == 0x0A
                && (bytes[6] & 0xFF) == 0x1A && (bytes[7] & 0xFF) == 0x0A) {
            return PNG;
        }
        if (bytes.length >= 12
                && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return WEBP;
        }
        return null;
    }

    public record StoredFile(String filename, String url, String contentType, long size) {
    }
}
