package org.resume.s3filemanager.exception;

public class FileUploadException extends RuntimeException {
    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, String fileName) {
        super(String.format(message, fileName));
    }
}
