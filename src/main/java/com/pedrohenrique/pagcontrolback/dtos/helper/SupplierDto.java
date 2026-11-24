package com.pedrohenrique.pagcontrolback.dtos.helper;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record SupplierDto(
        @JsonProperty("supplier_id")
        UUID supplierId,
        @JsonProperty("supplier_name")
        String supplierName
) {
}
