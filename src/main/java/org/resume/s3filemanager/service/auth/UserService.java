package org.resume.s3filemanager.service.auth;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.constant.ErrorMessages;
import org.resume.s3filemanager.entity.User;
import org.resume.s3filemanager.enums.FileUploadStatus;
import org.resume.s3filemanager.enums.UserRole;
import org.resume.s3filemanager.exception.UserAlreadyExistsException;
import org.resume.s3filemanager.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public void createUser(String username, String password) {
        createUserWithRole(username, password, UserRole.USER, FileUploadStatus.NOT_UPLOADED);
        log.info("User created: {}", username);
    }

    public void createAdmin(String username, String password) {
        createUserWithRole(username, password, UserRole.ADMIN, FileUploadStatus.UNLIMITED);
        log.info("Admin created in DB successfully: {}", username);
    }

    private void createUserWithRole(String username, String password,
                                    UserRole role, FileUploadStatus uploadStatus) {
        if (existsByUsername(username)) {
            throw new UserAlreadyExistsException(ErrorMessages.USER_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setUploadStatus(uploadStatus);

        userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(ErrorMessages.USER_NOT_FOUND, username)));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}
