package com.pedrohenrique.pagcontrolback.dtos.command;

import java.util.UUID;

public record CreateSupplierCommand(
        String name,
        String cnpj,
        UUID userId
) {
}
