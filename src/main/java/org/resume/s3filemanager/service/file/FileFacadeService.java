package org.resume.s3filemanager.service.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.dto.FileDownloadResponse;
import org.resume.s3filemanager.entity.FileMetadata;
import org.resume.s3filemanager.entity.User;
import org.resume.s3filemanager.exception.FileReadException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileFacadeService {

    private final FileHashService fileHashService;
    private final YandexStorageService fileStorageService;
    private final FileMetadataService fileMetadataService;
    private final FilePermissionService filePermissionService;

    public void uploadFile(MultipartFile file) {
        User user = filePermissionService.checkUploadPermission();

        String uniqueFileName = generateUniqueUUIDFileName(file.getOriginalFilename());
        byte[] fileBytes = readFileBytes(file);
        String fileHash = fileHashService.calculateMD5(fileBytes);

        fileHashService.checkDuplicateInDatabase(fileHash, user.getId());

        fileStorageService.uploadFileYandexS3(uniqueFileName, fileBytes, file.getContentType());

        try {
            fileMetadataService.saveFileWithPermission(file, uniqueFileName, fileHash, user);
            log.info("File uploaded successfully: {}", uniqueFileName);
        } catch (Exception e) {
            log.warn("DB save failed, rolling back S3 upload: {}", uniqueFileName);
            compensateS3Upload(uniqueFileName);
            throw e;
        }
    }

    public FileDownloadResponse downloadFile(String uniqueName) {
        byte[] data = fileStorageService.downloadFileYandexS3(uniqueName);

        FileMetadata metadata = fileMetadataService.findByUniqueName(uniqueName);

        String encodedFileName = URLEncoder.encode(
                metadata.getOriginalName(),
                StandardCharsets.UTF_8
        ).replace("+", "%20");


        return FileDownloadResponse.builder()
                .content(data)
                .fileName(encodedFileName)
                .contentType(metadata.getType())
                .size(data.length)
                .build();
    }

    public void deleteFile(String uniqueName) {
        User currentUser = filePermissionService.getCurrentUser();
        FileMetadata file = fileMetadataService.findByUniqueName(uniqueName);
        filePermissionService.checkDeletePermission(currentUser, file);

        fileStorageService.deleteFileYandexS3(uniqueName);

        fileMetadataService.deleteFileAndUpdateUserStatus(file);
        log.info("File deleted successfully: {}", uniqueName);
    }

    private byte[] readFileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            log.error("Failed to read file: {}", file.getOriginalFilename(), ex);
            throw new FileReadException(ex, file.getOriginalFilename());
        }
    }

    private void compensateS3Upload(String fileName) {
        try {
            fileStorageService.deleteFileYandexS3(fileName);
            log.info("Successfully rolled back S3 upload: {}", fileName);
        } catch (Exception ex) {
            log.error("Failed to rollback S3 upload: {}", fileName, ex);
        }
    }

    private String generateUniqueUUIDFileName(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        extension = (extension != null && !extension.isBlank()) ? extension : "tmp";
        return UUID.randomUUID() + "." + extension;
    }
}
