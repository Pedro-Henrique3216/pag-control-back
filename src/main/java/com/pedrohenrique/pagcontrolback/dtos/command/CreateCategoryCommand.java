package com.pedrohenrique.pagcontrolback.dtos.command;

import com.pedrohenrique.pagcontrolback.model.TransactionType;

import java.util.UUID;

public record CreateCategoryCommand(
        String name,
        TransactionType categoryType,
        UUID userId
) {
}
