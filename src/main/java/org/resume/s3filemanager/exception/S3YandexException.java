package org.resume.s3filemanager.exception;

public class S3YandexException extends RuntimeException {
    public S3YandexException(String message) {
        super(message);
    }

    public S3YandexException(String message, Throwable cause) {
        super(message, cause);
    }
}
