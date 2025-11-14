package org.resume.s3filemanager.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileResponse {
    private String fileName;
    private String fileSize;
}
