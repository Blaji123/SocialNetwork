package com.example.guiex1.domain.validators;

import com.example.guiex1.domain.Friendship;

public class FriendshipValidator implements Validator<Friendship> {
    /**
     * @param entity - Friendship
     * @throws ValidationException if one of the users given doesnt exist
     */
    @Override
    public void validate(Friendship entity) throws ValidationException {
        String errorMessage = "";
        if(entity.getId().getE1() == null || entity.getId().getE2() == null) {
            errorMessage += "User doesnt exist";
        }

        if(entity.getId().getE1().equals(entity.getId().getE2())) {
            errorMessage += "Users can't be the same";
        }

        if(!errorMessage.isEmpty())
            throw new ValidationException(errorMessage);

    }
}
