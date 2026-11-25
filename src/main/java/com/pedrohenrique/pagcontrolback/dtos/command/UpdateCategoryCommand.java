package com.pedrohenrique.pagcontrolback.dtos.command;

import com.pedrohenrique.pagcontrolback.model.TransactionType;

public record UpdateCategoryCommand(
        String name,
        TransactionType categoryType
) {
}
