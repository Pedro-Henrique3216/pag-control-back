package com.pedrohenrique.pagcontrolback.dtos.response;

import com.pedrohenrique.pagcontrolback.model.CategoryType;

import java.util.UUID;

public record CategoryResponseDto(
        UUID id,
        String name,
        CategoryType type
) {
}
