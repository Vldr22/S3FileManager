package org.resume.s3filemanager.setup;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.resume.s3filemanager.properties.AdminProperties;
import org.resume.s3filemanager.service.auth.UserService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(AdminProperties.class)
public class AdminInitializer {

    private final AdminProperties adminProperties;
    private final UserService userService;

    @PostConstruct
    public void createAdmin() {
        if (!userService.existsByUsername(adminProperties.getName())) {
            userService.createAdmin(adminProperties.getName(), adminProperties.getPassword());
        }
    }
}
