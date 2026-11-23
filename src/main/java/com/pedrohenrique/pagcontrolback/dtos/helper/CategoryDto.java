package com.pedrohenrique.pagcontrolback.dtos.helper;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record CategoryDto(
        @JsonProperty("category_id")
        UUID categoryId,
        @JsonProperty("category_name")
        String categoryName
) {
}
