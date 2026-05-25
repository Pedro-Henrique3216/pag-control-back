package com.pedrohenrique.pagcontrolback.dtos.request;

import com.pedrohenrique.pagcontrolback.model.InstallmentStatus;
import com.pedrohenrique.pagcontrolback.model.PaymentType;

import java.time.YearMonth;
import java.util.UUID;

public record ListInstallmentQuery(
        String search,
        YearMonth month,
        UUID supplierId,
        InstallmentStatus status,
        Boolean overdue,
        Boolean dueInNext7Days,
        PaymentType paymentType,
        UUID categoryId
) {
}
