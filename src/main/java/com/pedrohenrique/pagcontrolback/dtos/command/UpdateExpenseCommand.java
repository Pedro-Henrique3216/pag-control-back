package com.pedrohenrique.pagcontrolback.dtos.command;

import java.util.UUID;

public record UpdateExpenseCommand(
        String description,
        String invoiceNumber,
        UUID supplierId,
        UUID categoryId
) {
}