package org.resume.s3filemanager.dto;

import lombok.Builder;
import org.resume.s3filemanager.audit.AuditOperation;
import org.resume.s3filemanager.audit.ResourceType;
import org.resume.s3filemanager.enums.ResponseStatus;

import java.time.Instant;

@Builder
public record AuditLogResponse(Long id, String requestId, String username, String ipAddress, AuditOperation operation,
                               ResourceType resourceType, String resourceId, ResponseStatus status, String details,
                               Instant timestamp) {
}
