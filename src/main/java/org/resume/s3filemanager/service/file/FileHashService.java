package org.resume.s3filemanager.service.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.resume.s3filemanager.exception.DuplicateFileException;
import org.resume.s3filemanager.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileHashService {

    private final FileMetadataRepository fileMetadataRepository;

    public String calculateMD5(byte[] fileBytes) {
        return DigestUtils.md5DigestAsHex(fileBytes);
    }

    public void checkDuplicateInDatabase(String fileHash, Long userId) {
        if (fileMetadataRepository.existsByFileHashAndUserId(fileHash, userId)) {
            log.warn("Duplicate file detected with hash: {} for user: {}", fileHash, userId);
            throw new DuplicateFileException();
        }
    }
}
