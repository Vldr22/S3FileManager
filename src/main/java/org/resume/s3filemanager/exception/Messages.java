package org.resume.s3filemanager.exception;

public class Messages {

    private Messages() {
    }

    public static final String FILE_METADATA_NOT_FOUND = "File metadata not found";
    public static final String FILE_NOT_FOUND = "File not found: %s";
    public static final String FILE_UPLOAD_SUCCESS = "File uploaded successfully";
    public static final String FILE_UPLOAD_ERROR = "Failed to upload file";
    public static final String DUPLICATE_FILE_ERROR = "Duplicate file. File already exists";
    public static final String FILE_STORAGE_ERROR = "Unable to process the file operation in the storage";
}
