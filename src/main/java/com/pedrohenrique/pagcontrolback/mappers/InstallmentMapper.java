package com.pedrohenrique.pagcontrolback.mappers;

import com.pedrohenrique.pagcontrolback.dtos.request.InstallmentUpdateDto;
import com.pedrohenrique.pagcontrolback.dtos.response.InstallmentResponseDto;
import com.pedrohenrique.pagcontrolback.model.Installment;

public class InstallmentMapper {

    public static Installment toDomain(InstallmentUpdateDto installmentUpdateDto) {
        return new Installment(
                installmentUpdateDto.amount(),
                installmentUpdateDto.dueDate(),
                installmentUpdateDto.barcode()
        );
    }

    public static InstallmentResponseDto fromDomain(Installment installment) {
        return new InstallmentResponseDto(
                installment.getInstallmentId(),
                installment.getAmount(),
                installment.getDueDate(),
                installment.getBarcode(),
                installment.getStatus()
        );
    }
}
