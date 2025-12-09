package org.resume.s3filemanager.service.file;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.entity.FileMetadata;
import org.resume.s3filemanager.entity.User;
import org.resume.s3filemanager.enums.FileUploadStatus;
import org.resume.s3filemanager.exception.FileNotFoundException;
import org.resume.s3filemanager.repository.FileMetadataRepository;
import org.resume.s3filemanager.service.auth.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileMetadataService {

    private final FileMetadataRepository fileMetadataRepository;
    private final FilePermissionService fileUploadPermissionService;
    private final UserService userService;

    public void saveDatabaseMetadata(MultipartFile file, String uniqueName,
                                     String fileHash, User user) {
        FileMetadata metadata = FileMetadata.builder()
                .uniqueName(uniqueName)
                .originalName(file.getOriginalFilename())
                .type(file.getContentType())
                .size(file.getSize())
                .fileHash(fileHash)
                .user(user)
                .build();

        fileMetadataRepository.save(metadata);
    }

    @Transactional
    public void deleteFileAndUpdateUserStatus(FileMetadata file) {
        deleteDatabaseMetadata(file.getUniqueName());
        userService.updateUploadStatus(file.getUser(), FileUploadStatus.NOT_UPLOADED);
    }

    @Transactional
    public void saveFileWithPermission(MultipartFile file, String uniqueFileName, String fileHash, User user) {
        saveDatabaseMetadata(file, uniqueFileName, fileHash, user);
        fileUploadPermissionService.markFileUploaded();
    }

    @Transactional
    public void deleteDatabaseMetadata(String uniqueName) {
        int deleted = fileMetadataRepository.deleteByUniqueName(uniqueName);
        if (deleted == 0) {
            log.warn("File metadata not found for deletion: {}", uniqueName);
            throw new FileNotFoundException(uniqueName);
        }
    }

    public FileMetadata findByUniqueName(String uniqueName) {
        return fileMetadataRepository.findByUniqueName(uniqueName)
                .orElseThrow(() -> {
            log.warn("File not found: {}", uniqueName);
            return new FileNotFoundException(uniqueName);
        });
    }
}
