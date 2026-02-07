package com.pedrohenrique.pagcontrolback.mappers;

import com.pedrohenrique.pagcontrolback.dtos.request.SupplierRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.response.SupplierResponseDto;
import com.pedrohenrique.pagcontrolback.model.Supplier;

public class SupplierMapper {

    public static Supplier toDomain(SupplierRequestDto supplierRequestDto) {
        if (supplierRequestDto.cnpj() == null) {
            return new Supplier(
                    supplierRequestDto.name()
            );
        }
        return new Supplier(
                supplierRequestDto.name(),
                supplierRequestDto.cnpj()
        );
    }

    public static SupplierResponseDto fromDomain(Supplier supplier) {
        return new SupplierResponseDto(
                supplier.getId(),
                supplier.getName(),
                supplier.getCnpj()
        );
    }
}
