package org.resume.s3filemanager.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorMessages {

    public static final String USER_NOT_FOUND = "User not found with username: %s";
    public static final String USER_ALREADY_EXISTS = "User already exists";
    public static final String FILE_METADATA_NOT_FOUND = "File metadata not found";
    public static final String FILE_NOT_FOUND = "File not found: %s";
    public static final String FILE_UPLOAD_ERROR = "Failed to upload file";
    public static final String DUPLICATE_FILE_ERROR = "Duplicate file. File already exists";
    public static final String FILE_STORAGE_ERROR = "Unable to process the file operation in the storage";
    public static final String FILE_ALREADY_BEEN_UPLOADED = "File already been uploaded";

}
