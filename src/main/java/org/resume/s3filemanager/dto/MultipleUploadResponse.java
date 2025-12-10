package org.resume.s3filemanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.resume.s3filemanager.enums.ResponseStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MultipleUploadResponse(
        ResponseStatus status,
        String originalFileName,
        String uniqueName,
        String message) {
}