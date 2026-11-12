package com.construction.construction_management.controller;

import com.construction.construction_management.dto.FileUploadResponse;
import com.construction.construction_management.service.AccessGuard;
import com.construction.construction_management.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Duration;

@RestController
public class FileController {

    private final AccessGuard accessGuard;
    private final FileStorageService fileStorageService;

    public FileController(AccessGuard accessGuard, FileStorageService fileStorageService) {
        this.accessGuard = accessGuard;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Uploads one site photo. The caller must have access to the project, so a random
     * logged-in user cannot fill the server's disk with uploads.
     */
    @PostMapping("/api/projects/{projectId}/files")
    public ResponseEntity<FileUploadResponse> upload(@PathVariable int projectId,
                                                     @RequestParam("file") MultipartFile file) {
        accessGuard.projectForSiteRead(projectId);
        FileStorageService.StoredFile stored = fileStorageService.store(file, projectId);
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(stored.url())
                .toUriString();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new FileUploadResponse(stored.filename(), url, stored.contentType(), stored.size()));
    }

    /**
     * Serves an uploaded photo. Photos are intentionally public but unguessable so they can
     * be rendered in an {@code <img>} tag (browsers cannot attach a JWT to image requests).
     * Only image extensions are accepted, and the response is marked {@code nosniff}.
     */
    @GetMapping("/api/files/{filename:.+}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        Resource resource = fileStorageService.load(filename);
        String contentType = fileStorageService.contentType(filename);
        ContentDisposition disposition = ContentDisposition.inline().filename(filename).build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Security-Policy", "default-src 'none'; sandbox")
                .body(resource);
    }
}
