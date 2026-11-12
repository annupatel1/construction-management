package com.construction.construction_management.dto;

/**
 * Result of uploading a site photo. {@code url} is an absolute, unguessable URL that can be
 * stored in a progress-report or issue photo record and rendered by the browser.
 */
public record FileUploadResponse(String filename, String url, String contentType, long size) {
}
