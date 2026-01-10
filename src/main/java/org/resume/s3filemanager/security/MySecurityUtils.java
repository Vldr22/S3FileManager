package org.resume.s3filemanager.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Утилитарный класс для работы с Spring Security контекстом.
 * <p>
 * Предоставляет удобные методы для получения информации о текущем
 * аутентифицированном пользователе, его ролях и IP адресе клиента.
 */
@Slf4j
@UtilityClass
public class MySecurityUtils {

    private static final String ANONYMOUS_USER = "anonymousUser";
    private static final String UNKNOWN = "unknown";


    /**
     * Получает объект аутентификации из SecurityContext.
     *
     * @return текущая аутентификация или null
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Получает имя текущего аутентифицированного пользователя.
     *
     * @return имя пользователя или null для не аутентифицированных/анонимных пользователей
     */
    public static String getCurrentUsername() {
        Authentication auth = getCurrentAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();

        if (ANONYMOUS_USER.equals(principal)) {
            return null;
        }

        return principal instanceof String ? (String) principal : null;
    }

    /**
     * Получает IP адрес клиента с учетом прокси и балансировщиков.
     * <p>
     * Проверяет заголовки X-Forwarded-For и X-Real-IP перед использованием
     * RemoteAddr для корректного определения IP за прокси.
     *
     * @return IP адрес клиента или "unknown"
     */
    public static String getClientIp() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return UNKNOWN;
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }

        return request.getRemoteAddr();
    }

    private static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        return attributes != null ? attributes.getRequest() : null;
    }
}
