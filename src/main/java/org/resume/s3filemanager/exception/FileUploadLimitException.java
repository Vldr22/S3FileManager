package org.resume.s3filemanager.exception;

public class FileUploadLimitException extends RuntimeException {
    public FileUploadLimitException(String message) {
        super(message);
    }

    public FileUploadLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
