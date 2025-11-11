package org.resume.s3filemanager.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.dto.FileDownloadResponse;
import org.resume.s3filemanager.exception.FileUploadException;
import org.resume.s3filemanager.exception.Messages;
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

    @Transactional
    public void uploadFile(MultipartFile file) {
        String uniqueFileName = generateUniqueUUIDFileName(file.getOriginalFilename());

        try {
            byte[] bytes = file.getBytes();
            String fileHash = fileHashService.calculateMD5(bytes);
            fileHashService.checkDuplicateInDatabase(fileHash);

            fileMetadataService.saveDatabaseMetadata(file, uniqueFileName, fileHash);
            fileStorageService.uploadFileYandexS3(uniqueFileName, bytes, file.getContentType());

            log.info("File uploaded successfully: {}", uniqueFileName);
        } catch (IOException e) {
            log.error("I/O error processing file: {}", file.getOriginalFilename(), e);
            throw new FileUploadException(Messages.FILE_UPLOAD_ERROR, file.getOriginalFilename());
        } catch (Exception e) {
            log.error("Error uploading file: {}", file.getOriginalFilename(), e);
            throw new FileUploadException(Messages.FILE_UPLOAD_ERROR, file.getOriginalFilename());
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
