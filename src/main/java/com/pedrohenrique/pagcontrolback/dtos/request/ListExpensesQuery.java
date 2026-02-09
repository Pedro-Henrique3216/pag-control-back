package com.pedrohenrique.pagcontrolback.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.YearMonth;
import java.util.UUID;

public record ListExpensesQuery(
        @DateTimeFormat(pattern = "yyyy-MM")
        YearMonth month,
        @JsonProperty("supplier_id")
        UUID supplierId,
        @JsonProperty("invoice_number")
        String invoiceNumber
) {
}
