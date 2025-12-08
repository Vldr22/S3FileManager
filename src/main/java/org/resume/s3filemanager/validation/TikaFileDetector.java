package org.resume.s3filemanager.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TikaFileDetector {

    private final Tika tika;

    public String detectContentType(byte[] fileBytes, String fileName) {
        String detectedType = tika.detect(fileBytes, fileName);
        log.debug("Detected content type: {} for file: {}", detectedType, fileName);
        return detectedType;
    }

    public boolean verifyContentType(byte[] fileBytes, String fileName, String declaredContentType) {
        String normalizedRealType = normalizeContentType(detectContentType(fileBytes, fileName));
        String normalizedDeclaredType = normalizeContentType(declaredContentType);

        if (normalizedRealType.equalsIgnoreCase(normalizedDeclaredType)) {
            return true;
        }

        log.warn("Content type mismatch for file {}: real={}, declared={}",
                fileName, normalizedRealType, normalizedDeclaredType);
        return false;

    }

    private String normalizeContentType(String contentType) {
        MediaType mediaType = MediaType.parseMediaType(contentType);
        return String.format("%s/%s", mediaType.getType(), mediaType.getSubtype());
    }
}
