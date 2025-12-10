package org.resume.s3filemanager.service.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.constant.ErrorMessages;
import org.resume.s3filemanager.constant.SuccessMessages;
import org.resume.s3filemanager.constant.ValidationMessages;
import org.resume.s3filemanager.dto.FileDownloadResponse;
import org.resume.s3filemanager.dto.MultipleUploadResponse;
import org.resume.s3filemanager.entity.FileMetadata;
import org.resume.s3filemanager.entity.User;
import org.resume.s3filemanager.enums.ResponseStatus;
import org.resume.s3filemanager.exception.*;
import org.resume.s3filemanager.properties.FileUploadProperties;
import org.resume.s3filemanager.validation.FileValidator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(FileUploadProperties.class)
public class FileFacadeService {

    private final FileHashService fileHashService;
    private final YandexStorageService fileStorageService;
    private final FileMetadataService fileMetadataService;
    private final FilePermissionService filePermissionService;
    private final FileValidator fileValidator;
    private final FileUploadProperties fileUploadProperties;

    // Публичный API
    public void uploadFile(MultipartFile file) {
        User user = filePermissionService.checkUploadPermission();
        uploadFileInternal(file, user);
    }

    public List<MultipleUploadResponse> multipleUpload(MultipartFile[] files) {
        validateBatchUpload(files);
        User admin = filePermissionService.checkUploadPermission();

        List<MultipleUploadResponse> results = new ArrayList<>();
        int successCount = 0;

        for (MultipartFile file : files) {
            MultipleUploadResponse response = processSingleFile(file, admin);
            results.add(response);

            if (response.status() == ResponseStatus.SUCCESS) {
                successCount++;
            }
        }

        if (successCount == 0) {
            throw new MultipleFileUploadException(results);
        }

        log.info("Batch upload completed: {}/{} successful", successCount, files.length);
        return results;
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

    // Основная логика
    private String uploadFileInternal(MultipartFile file, User user) {
        String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());
        byte[] fileBytes = readFileBytes(file);
        String fileHash = fileHashService.calculateMD5(fileBytes);

        fileHashService.checkDuplicateInDatabase(fileHash, user.getId());
        fileStorageService.uploadFileYandexS3(uniqueFileName, fileBytes, file.getContentType());

        try {
            fileMetadataService.saveFileWithPermission(file, uniqueFileName, fileHash, user);
            log.info("File uploaded successfully: {}", uniqueFileName);
            return uniqueFileName;
        } catch (Exception e) {
            log.warn("DB save failed, rolling back S3 upload: {}", uniqueFileName);
            compensateS3Upload(uniqueFileName);
            throw e;
        }
    }

    private MultipleUploadResponse processSingleFile(MultipartFile file, User admin) {
        try {

            Optional<String> validationError = fileValidator.validateFile(file);
            if (validationError.isPresent()) {
                return createValidationErrorResponse(file, validationError.get());
            }

            String uniqueName = uploadFileInternal(file, admin);
            return createSuccessResponse(file, uniqueName);

        } catch (Exception e) {
            return createExceptionErrorResponse(file, e);
        }
    }

    // VALIDATION
    private void validateBatchUpload(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException(ValidationMessages.FILE_EMPTY);
        }

        if (files.length > fileUploadProperties.getMaxBatchSize()) {
            throw new TooManyFilesException(fileUploadProperties.getMaxBatchSize());
        }
    }

    // Multiple-upload responses
    private MultipleUploadResponse createSuccessResponse(MultipartFile file, String uniqueName) {
        return new MultipleUploadResponse(
                ResponseStatus.SUCCESS,
                file.getOriginalFilename(),
                uniqueName,
                SuccessMessages.FILE_UPLOAD_SUCCESS
        );
    }

    private MultipleUploadResponse createValidationErrorResponse(MultipartFile file, String errorMessage) {
        log.warn("Validation failed for {}: {}", file.getOriginalFilename(), errorMessage);
        return new MultipleUploadResponse(
                ResponseStatus.ERROR,
                file.getOriginalFilename(),
                null,
                errorMessage
        );
    }

    private MultipleUploadResponse createExceptionErrorResponse(MultipartFile file, Exception e) {
        String errorMessage = switch (e) {
            case DuplicateFileException ignored -> {
                log.warn("Duplicate file: {}", file.getOriginalFilename());
                yield ErrorMessages.FILE_ALREADY_BEEN_UPLOADED;
            }
            case FileReadException ignored -> {
                log.error("File read error: {}", file.getOriginalFilename(), e);
                yield ErrorMessages.FILE_READ_ERROR;
            }
            case S3YandexException ignored -> {
                log.error("S3 storage error: {}", file.getOriginalFilename(), e);
                yield ErrorMessages.FILE_STORAGE_ERROR;
            }
            default -> {
                log.error("Unexpected error uploading file: {}", file.getOriginalFilename(), e);
                yield ErrorMessages.UNEXPECTED_ERROR;
            }
        };

        return new MultipleUploadResponse(
                ResponseStatus.ERROR,
                file.getOriginalFilename(),
                null,
                errorMessage
        );
    }

    // Helper methods
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

    private String generateUniqueFileName(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        extension = (extension != null && !extension.isBlank()) ? extension : "tmp";
        return UUID.randomUUID() + "." + extension;
    }
}
