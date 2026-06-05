package com.pedrohenrique.pagcontrolback.mappers;

import com.pedrohenrique.pagcontrolback.dtos.response.UserResponseDto;
import com.pedrohenrique.pagcontrolback.model.User;

public class UserMapper {

    public static UserResponseDto toResponse(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getFantasyName(),
                user.getEmail().value(),
                user.getPhone().value(),
                user.getPersonType()
        );
    }
}
