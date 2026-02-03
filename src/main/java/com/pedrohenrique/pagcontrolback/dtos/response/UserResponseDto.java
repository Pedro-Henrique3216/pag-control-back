package com.pedrohenrique.pagcontrolback.dtos.response;

import com.fasterxml.jackson.annotation.*;
import com.pedrohenrique.pagcontrolback.model.PersonType;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String name,
        @JsonProperty(value = "fantasy_name")
        String fantasyName,
        String email,
        String phone,
        @JsonProperty(value = "person_type")
        PersonType personType
) {
}
