package org.resume.s3filemanager.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.resume.s3filemanager.exception.DuplicateFileException;
import org.resume.s3filemanager.exception.Messages;
import org.resume.s3filemanager.repository.FileMetadataRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileHashService {

    private final FileMetadataRepository fileMetadataRepository;

    public String calculateMD5(byte[] fileBytes) {
        return DigestUtils.md5Hex(fileBytes);
    }

    public void checkDuplicateInDatabase(String fileHash) {
        if (fileMetadataRepository.existsByFileHash(fileHash)) {
            log.error("Duplicate file hash found fileHash: {}", fileHash);
            throw new DuplicateFileException(Messages.DUPLICATE_FILE_ERROR);
        }
    }
}
