package org.resume.s3filemanager.controller;

import lombok.RequiredArgsConstructor;
import org.resume.s3filemanager.audit.AuditOperation;
import org.resume.s3filemanager.dto.AuditLogFilterRequest;
import org.resume.s3filemanager.dto.AuditLogResponse;
import org.resume.s3filemanager.enums.ResponseStatus;
import org.resume.s3filemanager.service.admin.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * REST контроллер для административных операций.
 * <p>
 * Предоставляет API для просмотра журнала аудита с поддержкой
 * фильтрации и пагинации. Доступен только администраторам.
 *
 * @see AdminService
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * Возвращает журнал аудита с фильтрацией и пагинацией.
     * <p>
     * Поддерживаемые фильтры (все опциональные):
     * <ul>
     *   <li>Username - фильтр по имени пользователя</li>
     *   <li>Operation - фильтр по типу операции</li>
     *   <li>Status - фильтр по успешных и неуспешных операций</li>
     *   <li>From - начало временного диапазона</li>
     *   <li>To - конец временного диапазона</li>
     * </ul>
     *
     * @param username имя пользователя (опционально)
     * @param operation тип операции (опционально)
     * @param status статус операции (опционально)
     * @param from начало диапазона (опционально)
     * @param to конец диапазона (опционально)
     * @param pageable параметры пагинации (page, size, sort)
     * @return страница с записями аудита
     */
    @GetMapping("/audit-logs")
    public Page<AuditLogResponse> getAuditLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) AuditOperation operation,
            @RequestParam(required = false) ResponseStatus status,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            Pageable pageable) {

        AuditLogFilterRequest filter = new AuditLogFilterRequest(username, operation, status, from, to);
        return adminService.getAuditLogs(filter, pageable);
    }
}
