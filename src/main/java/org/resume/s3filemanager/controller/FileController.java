package org.resume.s3filemanager.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.resume.s3filemanager.constant.SuccessMessages;
import org.resume.s3filemanager.dto.CommonResponse;
import org.resume.s3filemanager.dto.FileDownloadResponse;
import org.resume.s3filemanager.service.file.FileFacadeService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileFacadeService fileFacadeService;
    private final static String FILENAME_NOT_EMPTY = "filename cannot be empty";

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<String> upload (@RequestParam("file")
            MultipartFile file) {
        fileFacadeService.uploadFile(file);
        return CommonResponse.success(SuccessMessages.FILE_UPLOAD_SUCCESS);
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<ByteArrayResource> download(
            @PathVariable
            @NotBlank(message = FILENAME_NOT_EMPTY)
            String fileName) {

        FileDownloadResponse response = fileFacadeService.downloadFile(fileName);
        ByteArrayResource resource = new ByteArrayResource(response.getContent());

        return ResponseEntity
                .ok()
                .contentLength(response.getSize())
                .header("Content-Type", response.getContentType())
                .header("Content-Disposition", "attachment; filename*=UTF-8''" + response.getFileName())
                .body(resource);
    }

    @DeleteMapping("/{fileName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String fileName) {
        fileFacadeService.deleteFile(fileName);
    }
}
