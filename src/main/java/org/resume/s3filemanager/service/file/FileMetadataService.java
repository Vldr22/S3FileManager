package org.resume.s3filemanager.service.file;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.entity.FileMetadata;
import org.resume.s3filemanager.constant.ErrorMessages;
import org.resume.s3filemanager.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileMetadataService {

    private final FileMetadataRepository fileMetadataRepository;

    public void saveDatabaseMetadata(MultipartFile file, String uniqueName, String fileHash) {
        FileMetadata metadata = FileMetadata.builder()
                .uniqueName(uniqueName)
                .originalName(file.getOriginalFilename())
                .type(file.getContentType())
                .size(file.getSize())
                .fileHash(fileHash)
                .build();

        fileMetadataRepository.save(metadata);
        log.info("Saving database metadata: {}", metadata);
    }

    @Transactional
    public void deleteDatabaseMetadata(String uniqueName) {
        int deleted = fileMetadataRepository.deleteByUniqueName(uniqueName);
        if (deleted == 0) {
            log.warn("Metadata not found for deletion: {}", uniqueName);
            throw new EntityNotFoundException(ErrorMessages.FILE_METADATA_NOT_FOUND);
        }
        log.info("Deleted file metadata with uniqueName: {}", uniqueName);
    }

    public String getUniqueNameByOriginalFilename(String originalFileName) {
        FileMetadata fileMetadata = fileMetadataRepository.findByOriginalName(originalFileName)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(ErrorMessages.FILE_NOT_FOUND, originalFileName)));
        return fileMetadata.getUniqueName();
    }
}
