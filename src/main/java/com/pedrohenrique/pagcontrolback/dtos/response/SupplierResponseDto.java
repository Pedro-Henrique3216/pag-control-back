package com.pedrohenrique.pagcontrolback.dtos.response;

import java.util.UUID;

public record SupplierResponseDto(
        UUID id,
        String name,
        String cnpj
) {
}
