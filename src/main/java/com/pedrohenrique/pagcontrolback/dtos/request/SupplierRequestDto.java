package com.pedrohenrique.pagcontrolback.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record SupplierRequestDto(
        @NotBlank(message = "name must not be blank")
        String name,
        String cnpj
) {

}
