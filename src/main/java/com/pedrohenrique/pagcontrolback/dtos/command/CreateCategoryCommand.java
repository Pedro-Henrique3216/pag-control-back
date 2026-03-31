package com.pedrohenrique.pagcontrolback.dtos.command;

import com.pedrohenrique.pagcontrolback.model.CategoryType;

import java.util.UUID;

public record CreateCategoryCommand(
        String name,
        CategoryType categoryType,
        UUID userId
) {
}
