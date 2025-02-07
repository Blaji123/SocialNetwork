package com.example.guiex1.domain.validators;

import com.example.guiex1.domain.User;

import java.util.regex.Pattern;

public class UserValidator implements Validator<User> {
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(com|net|org|edu|gov|io|co|info|me|biz)$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @Override
    public void validate(User entity) throws ValidationException {
        String errorMessage = "";

        if(entity.getFirstName().isEmpty())
            errorMessage += "First name can't be empty.";

        if(entity.getLastName().isEmpty())
            errorMessage += "Last name can't be empty.";

        if(entity.getEmail().isEmpty() || !EMAIL_PATTERN.matcher(entity.getEmail()).matches())
            errorMessage += "Email is not valid.";

        if(entity.getPassword().length() < 8
                || !Pattern.compile("[A-Z]").matcher(entity.getPassword()).find()
                || !Pattern.compile("[!@#$%^&*(),.?\":{}|<>]").matcher(entity.getPassword()).find()
                || !Pattern.compile("\\d").matcher(entity.getPassword()).find()){
            errorMessage += "Password is not valid.";
        }

        if(!errorMessage.isEmpty())
            throw new ValidationException(errorMessage);
    }
}
