package org.resume.s3filemanager.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.resume.s3filemanager.constant.ValidationMessages;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    private final TikaFileDetector tikaFileDetector;

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return addViolation(context, ValidationMessages.FILE_EMPTY);
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            return addViolation(context, ValidationMessages.FILE_TYPE_UNKNOWN);
        }

        String extension = StringUtils.getFilenameExtension(filename);
        if (extension == null || extension.isBlank()) {
            return addViolation(context, ValidationMessages.FILE_TYPE_UNKNOWN);
        }
        extension = extension.toLowerCase();

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            return addViolation(context, ValidationMessages.FILE_TYPE_UNKNOWN);
        }

        if (!AllowedFileType.isAllowed(extension, contentType)) {
            return addViolation(context,
                    String.format(ValidationMessages.FILE_TYPE_NOT_ALLOWED, extension, contentType));
        }

        return validateRealContentType(file, contentType, filename, extension, context);
    }



    private boolean validateRealContentType(MultipartFile file, String declaredContentType,
                                            String filename, String extension, ConstraintValidatorContext context) {
        try {
            byte[] fileBytes = file.getBytes();
            boolean isValid = tikaFileDetector.verifyContentType(
                    fileBytes,
                    filename,
                    declaredContentType
            );

            if (!isValid) {
                return addViolation(context,
                        String.format(ValidationMessages.FILE_SIGNATURE_MISMATCH, extension));
            }

            return true;

        } catch (IOException e) {
            log.error("Error reading file: {}", filename, e);
            return addViolation(context, ValidationMessages.FILE_PROCESSING_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error during validation: {}", filename, e);
            return addViolation(context, ValidationMessages.FILE_PROCESSING_ERROR);
        }
    }

    private boolean addViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
        return false;
    }
}
