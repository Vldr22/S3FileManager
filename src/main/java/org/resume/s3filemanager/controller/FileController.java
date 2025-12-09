package org.resume.s3filemanager.controller;

import lombok.RequiredArgsConstructor;
import org.resume.s3filemanager.constant.SuccessMessages;
import org.resume.s3filemanager.dto.CommonResponse;
import org.resume.s3filemanager.dto.FileDownloadResponse;
import org.resume.s3filemanager.service.file.FileFacadeService;
import org.resume.s3filemanager.validation.ValidFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileFacadeService fileFacadeService;

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<String> upload (@RequestParam("file")
            @ValidFile MultipartFile file) {
        fileFacadeService.uploadFile(file);
        return CommonResponse.success(SuccessMessages.FILE_UPLOAD_SUCCESS);
    }

    @GetMapping("/{uniqueName}")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String uniqueName) {

        FileDownloadResponse response = fileFacadeService.downloadFile(uniqueName);
        ByteArrayResource resource = new ByteArrayResource(response.getContent());

        return ResponseEntity
                .ok()
                .contentLength(response.getSize())
                .header("Content-Type", response.getContentType())
                .header("Content-Disposition", "attachment; filename*=UTF-8''" + response.getFileName())
                .body(resource);
    }

    @DeleteMapping("/{uniqueName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String uniqueName) {
        fileFacadeService.deleteFile(uniqueName);
    }
}
