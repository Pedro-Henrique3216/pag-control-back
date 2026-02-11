package com.pedrohenrique.pagcontrolback.dtos.request;

import com.pedrohenrique.pagcontrolback.model.InstallmentStatus;

import java.time.YearMonth;
import java.util.UUID;

public record ListInstallmentQuery(
        YearMonth month,
        UUID supplierId,
        InstallmentStatus status,
        Boolean overdue,
        Boolean dueInNext7Days
) {
}
