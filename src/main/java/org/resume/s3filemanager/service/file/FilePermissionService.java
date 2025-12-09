package org.resume.s3filemanager.service.file;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.entity.FileMetadata;
import org.resume.s3filemanager.entity.User;
import org.resume.s3filemanager.enums.FileUploadStatus;
import org.resume.s3filemanager.enums.UserRole;
import org.resume.s3filemanager.exception.FileAccessDeniedException;
import org.resume.s3filemanager.exception.FileUploadLimitException;
import org.resume.s3filemanager.exception.UserNotFoundException;
import org.resume.s3filemanager.repository.UserRepository;
import org.resume.s3filemanager.security.MySecurityUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilePermissionService {

    private final UserRepository userRepository;

    public User checkUploadPermission() {
        User user = getCurrentUser();

        if (user.getUploadStatus() == FileUploadStatus.UNLIMITED) {
            return user;
        }

        if (user.getUploadStatus() == FileUploadStatus.FILE_UPLOADED) {
            log.warn("Upload denied: user {} already uploaded file", user.getUsername());
            throw new FileUploadLimitException();
        }

        log.debug("Upload permission granted for user: {}", user.getUsername());
        return user;
    }

    public void checkDeletePermission(User currentUser, FileMetadata file) {
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        boolean isOwner = file.getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new FileAccessDeniedException();
        }
    }

    @Transactional
    public void markFileUploaded() {
        User user = getCurrentUser();

        if (user.getUploadStatus() == FileUploadStatus.NOT_UPLOADED) {
            user.setUploadStatus(FileUploadStatus.FILE_UPLOADED);
            userRepository.save(user);
        }
    }

    public User getCurrentUser() {
        String username = MySecurityUtils.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Current user not found: {}", username);
                    return new UserNotFoundException(username);
                });
    }
}
