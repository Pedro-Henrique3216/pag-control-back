package com.pedrohenrique.pagcontrolback.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ExpenseUpdateDto(
        String description,
        @JsonProperty("invoice_number")
        String invoiceNumber,
        @JsonProperty("supplier_id")
        UUID supplierId,
        @JsonProperty("category_id")
        UUID categoryId
) {
}
