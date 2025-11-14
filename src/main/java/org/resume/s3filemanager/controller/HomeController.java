package org.resume.s3filemanager.controller;

import lombok.RequiredArgsConstructor;
import org.resume.s3filemanager.dto.FileResponse;
import org.resume.s3filemanager.service.FilePaginationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/home")
@RequiredArgsConstructor
public class HomeController {

    private final FilePaginationService filePaginationService;

    @GetMapping()
    public Page<FileResponse> getFiles(Pageable pageable) {
        return filePaginationService.paginate(pageable);
    }
}
