package org.resume.s3filemanager.service.admin;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.resume.s3filemanager.dto.AuditLogFilterRequest;
import org.resume.s3filemanager.dto.AuditLogResponse;
import org.resume.s3filemanager.entity.QAuditLog;
import org.resume.s3filemanager.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Сервис для административных операций.
 * <p>
 * Предоставляет функционал для работы с журналом аудита,
 * включая фильтрацию и пагинацию записей.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Возвращает отфильтрованный список записей аудит-логов. Использует пагинацию.
     * <p>
     * Поддерживает динамическую фильтрацию через Querydsl.
     * Все параметры фильтра опциональны — если не указаны, возвращаются все записи.
     *
     * @param filter   параметры фильтрации (username, operation, from, to)
     * @param pageable параметры пагинации и сортировки
     * @return страница с записями аудита
     */
    public Page<AuditLogResponse> getAuditLogs(AuditLogFilterRequest filter, Pageable pageable) {
        QAuditLog qAuditLog = QAuditLog.auditLog;
        BooleanBuilder builder = new BooleanBuilder();

        if (filter.username() != null) {
            builder.and(qAuditLog.username.eq(filter.username()));
        }
        if (filter.operation() != null) {
            builder.and(qAuditLog.operation.eq(filter.operation()));
        }
        if (filter.status() != null) {
            builder.and(qAuditLog.status.eq(filter.status()));
        }
        if (filter.from() != null) {
            builder.and(qAuditLog.timestamp.goe(filter.from()));
        }
        if (filter.to() != null) {
            builder.and(qAuditLog.timestamp.loe(filter.to()));
        }

        return auditLogRepository.findAll(builder, pageable)
                .map(this::toResponse);
    }

    private AuditLogResponse toResponse(org.resume.s3filemanager.entity.AuditLog entity) {
        return new AuditLogResponse(
                entity.getId(),
                entity.getRequestId(),
                entity.getUsername(),
                entity.getIpAddress() != null ? entity.getIpAddress().getAddress() : null,
                entity.getOperation(),
                entity.getResourceType(),
                entity.getResourceId(),
                entity.getStatus(),
                entity.getDetails(),
                entity.getTimestamp()
        );
    }

}
