package org.resume.s3filemanager.exception;

import lombok.Getter;
import org.resume.s3filemanager.constant.ErrorMessages;

@Getter
public class UserNotFoundException extends RuntimeException {

    private final String username;

    public UserNotFoundException(String username) {
        super(ErrorMessages.USER_NOT_FOUND);
        this.username = username;
    }

}
