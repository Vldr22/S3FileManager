package org.resume.s3filemanager.service.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.dto.FileDownloadResponse;
import org.springframework.transaction.annotation.Transactional;
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
    private final FileUploadPermissionService fileUploadPermissionService;

    public void uploadFile(MultipartFile file) {
        fileUploadPermissionService.checkUploadPermission();

        String uniqueFileName = generateUniqueUUIDFileName(file.getOriginalFilename());
        byte[] fileBytes = readFileBytes(file);
        String fileHash = fileHashService.calculateMD5(fileBytes);

        fileHashService.checkDuplicateInDatabase(fileHash);

        fileStorageService.uploadFileYandexS3(uniqueFileName, fileBytes, file.getContentType());

        try {
            fileMetadataService.saveFileWithPermission(file, uniqueFileName, fileHash);
            log.info("File uploaded successfully: {}", uniqueFileName);
        } catch (Exception e) {
            log.warn("DB save failed, rolling back S3 upload: {}", uniqueFileName);
            compensateS3Upload(uniqueFileName);
            throw e;
        }
    }

    public FileDownloadResponse downloadFile(String fileName) {
        byte[] data = fetchFileBytes(fileName);

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        return FileDownloadResponse.builder()
                .content(data)
                .fileName(encodedFileName)
                .contentType("application/octet-stream")
                .size(data.length)
                .build();
    }

    @Transactional
    public void deleteFile(String fileName) {
        String uniqueName = fileMetadataService.getUniqueNameByOriginalFilename(fileName);
        fileMetadataService.deleteDatabaseMetadata(uniqueName);
        fileStorageService.deleteFileYandexS3(uniqueName);
        log.info("File {} deleted successfully from S3 and DB", fileName);
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

    private byte[] fetchFileBytes(String fileName) {
        String uniqueName = fileMetadataService.getUniqueNameByOriginalFilename(fileName);
        return fileStorageService.downloadFileYandexS3(uniqueName);
    }
}
