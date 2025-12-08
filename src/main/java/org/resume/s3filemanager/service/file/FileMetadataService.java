package org.resume.s3filemanager.service.file;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.entity.FileMetadata;
import org.resume.s3filemanager.exception.FileNotFoundException;
import org.resume.s3filemanager.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileMetadataService {

    private final FileMetadataRepository fileMetadataRepository;
    private final FileUploadPermissionService fileUploadPermissionService;

    public void saveDatabaseMetadata(MultipartFile file, String uniqueName, String fileHash) {
        FileMetadata metadata = FileMetadata.builder()
                .uniqueName(uniqueName)
                .originalName(file.getOriginalFilename())
                .type(file.getContentType())
                .size(file.getSize())
                .fileHash(fileHash)
                .build();

        fileMetadataRepository.save(metadata);
    }

    @Transactional
    public void saveFileWithPermission(MultipartFile file, String uniqueFileName, String fileHash) {
        saveDatabaseMetadata(file, uniqueFileName, fileHash);
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

    public String getUniqueNameByOriginalFilename(String originalFileName) {
        FileMetadata fileMetadata = fileMetadataRepository.findByOriginalName(originalFileName)
                .orElseThrow(() -> {
                    log.warn("File not found: {}", originalFileName);
                    return new FileNotFoundException(originalFileName);
                });
        return fileMetadata.getUniqueName();
    }
}
