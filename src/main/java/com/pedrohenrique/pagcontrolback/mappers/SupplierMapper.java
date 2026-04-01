package com.pedrohenrique.pagcontrolback.mappers;

import com.pedrohenrique.pagcontrolback.dtos.response.SupplierResponseDto;
import com.pedrohenrique.pagcontrolback.model.Supplier;

public class SupplierMapper {

    public static SupplierResponseDto fromDomain(Supplier supplier) {
        return new SupplierResponseDto(
                supplier.getId(),
                supplier.getName(),
                supplier.getCnpj()
        );
    }
}
