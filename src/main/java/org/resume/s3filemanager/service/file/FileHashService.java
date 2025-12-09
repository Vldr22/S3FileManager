package org.resume.s3filemanager.service.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.resume.s3filemanager.exception.DuplicateFileException;
import org.resume.s3filemanager.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileHashService {

    private final FileMetadataRepository fileMetadataRepository;

    public String calculateMD5(byte[] fileBytes) {
        return DigestUtils.md5DigestAsHex(fileBytes);
    }

    @Transactional(readOnly = true)
    public void checkDuplicateInDatabase(String fileHash) {
        if (fileMetadataRepository.existsByFileHash(fileHash)) {
            log.warn("Duplicate file with hash: {}", fileHash);
            throw new DuplicateFileException();
        }
    }
}
