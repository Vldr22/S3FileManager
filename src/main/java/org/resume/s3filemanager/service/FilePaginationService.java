package org.resume.s3filemanager.service;

import lombok.RequiredArgsConstructor;
import org.resume.s3filemanager.dto.FileResponse;
import org.resume.s3filemanager.entity.FileMetadata;
import org.resume.s3filemanager.repository.FileMetadataRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FilePaginationService {

    private final FileMetadataRepository fileMetadataRepository;
    private final static String MB_SUFFIX = " MB";

    public Page<FileResponse> paginate(Pageable pageable) {
        Page<FileMetadata> fileMetadataPage = fileMetadataRepository.findAll(pageable);
        return fileMetadataPage.map(
                file -> FileResponse.builder()
                        .fileName(file.getOriginalName())
                        .fileSize(convertToMB(file.getSize()))
                        .build());
    }

    private String convertToMB(long bytes) {
        double mb = bytes / 1_048_576.0;
        return String.format("%.2f", mb) + MB_SUFFIX;
    }
}
