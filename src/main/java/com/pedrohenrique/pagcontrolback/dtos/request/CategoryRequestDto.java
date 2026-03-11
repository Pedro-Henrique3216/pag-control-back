package com.pedrohenrique.pagcontrolback.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pedrohenrique.pagcontrolback.model.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryRequestDto(
        @NotBlank(message = "Name is required.")
        String name,
        @NotNull(message = "Category type is required.")
        @JsonProperty(value = "category_type")
        CategoryType categoryType
) {
}
