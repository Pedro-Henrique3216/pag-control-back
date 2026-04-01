package com.pedrohenrique.pagcontrolback.dtos.command;

import com.pedrohenrique.pagcontrolback.model.PersonType;

public record CreateUserCommand(
        String name,
        String fantasyName,
        String email,
        String password,
        String phone,
        PersonType personType
) {
}
