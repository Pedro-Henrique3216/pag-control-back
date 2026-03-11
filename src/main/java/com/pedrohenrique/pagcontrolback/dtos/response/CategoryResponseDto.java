package com.pedrohenrique.pagcontrolback.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pedrohenrique.pagcontrolback.model.CategoryType;

import java.util.UUID;

public record CategoryResponseDto(
        UUID id,
        String name,
        @JsonProperty(value = "category_type")
        CategoryType categoryType
) {
}
