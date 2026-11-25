package com.pedrohenrique.pagcontrolback.dtos.command;

public record UpdateSupplierCommand(
        String name,
        String cnpj
) {
}
