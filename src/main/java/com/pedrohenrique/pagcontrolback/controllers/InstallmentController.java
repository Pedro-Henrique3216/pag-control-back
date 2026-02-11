package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.ListInstallmentQuery;
import com.pedrohenrique.pagcontrolback.dtos.response.InstallmentResponseDto;
import com.pedrohenrique.pagcontrolback.mappers.InstallmentMapper;
import com.pedrohenrique.pagcontrolback.model.Installment;
import com.pedrohenrique.pagcontrolback.model.InstallmentStatus;
import com.pedrohenrique.pagcontrolback.usecases.ListInstallmentsUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/installments")
public class InstallmentController {

    private final ListInstallmentsUseCase listInstallmentsUseCase;

    public InstallmentController(ListInstallmentsUseCase listInstallmentsUseCase) {
        this.listInstallmentsUseCase = listInstallmentsUseCase;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<InstallmentResponseDto>> getExpenses(
            @PathVariable
            UUID userId,

            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM")
            YearMonth month,

            @RequestParam(required = false, name = "supplier_id")
            UUID supplierId,

            @RequestParam(required = false)
            InstallmentStatus status,

            @RequestParam(required = false)
            Boolean overdue,

            @RequestParam(required = false, name = "due_in_next_days")
            Boolean dueInNext7Days
    ) {
        ListInstallmentQuery query = new ListInstallmentQuery(month, supplierId, status, overdue, dueInNext7Days);
        List<Installment> installments = listInstallmentsUseCase.execute(userId, query);
        List<InstallmentResponseDto> response = installments.stream()
                .map(InstallmentMapper::fromDomain)
                .toList();
        return ResponseEntity.ok(response);
    }
}
