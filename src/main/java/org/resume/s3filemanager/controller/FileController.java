package org.resume.s3filemanager.controller;

import lombok.RequiredArgsConstructor;
import org.resume.s3filemanager.service.YandexStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/files")
@RequiredArgsConstructor
public class FileController {

    private final YandexStorageService yandexStorageService;

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> upload (@RequestParam("file")
            MultipartFile file) {

        yandexStorageService.uploadFileYandexS3(file);
        return ResponseEntity
                .ok()
                .build();
    }
}
