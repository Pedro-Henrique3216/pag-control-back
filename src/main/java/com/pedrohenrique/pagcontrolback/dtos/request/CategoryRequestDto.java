package com.pedrohenrique.pagcontrolback.dtos.request;

import com.pedrohenrique.pagcontrolback.model.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryRequestDto(
        @NotBlank(message = "Name is required.")
        String name,
        @NotNull(message = "Category type is required.")
        CategoryType categoryType
) {
}
