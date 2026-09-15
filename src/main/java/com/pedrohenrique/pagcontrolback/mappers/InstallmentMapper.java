package com.pedrohenrique.pagcontrolback.mappers;

import com.pedrohenrique.pagcontrolback.dtos.response.InstallmentResponseDto;
import com.pedrohenrique.pagcontrolback.model.Installment;

public class InstallmentMapper {

    public static InstallmentResponseDto fromDomain(Installment installment) {
        return new InstallmentResponseDto(
                installment.getInstallmentId(),
                installment.getExpense().getDescription(),
                installment.getAmount().value(),
                installment.getDueDate(),
                installment.getBarcode(),
                installment.getStatus(),
                installment.getInstallmentNumber(),
                transformTotalInstallment(installment.getTotalInstallments())
        );
    }

    private static String transformTotalInstallment(Integer totalInstallments) {
        if (totalInstallments == null) {
            return "recorrente, sem contagem fixa";
        }

        return totalInstallments.toString();
    }
}
