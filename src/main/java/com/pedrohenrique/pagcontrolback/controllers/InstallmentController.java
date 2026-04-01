package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.config.security.UserPrincipal;
import com.pedrohenrique.pagcontrolback.dtos.command.UpdateInstallmentCommand;
import com.pedrohenrique.pagcontrolback.dtos.request.InstallmentUpdateDto;
import com.pedrohenrique.pagcontrolback.dtos.request.ListInstallmentQuery;
import com.pedrohenrique.pagcontrolback.dtos.response.InstallmentResponseDto;
import com.pedrohenrique.pagcontrolback.mappers.InstallmentMapper;
import com.pedrohenrique.pagcontrolback.model.Installment;
import com.pedrohenrique.pagcontrolback.model.InstallmentStatus;
import com.pedrohenrique.pagcontrolback.usecases.ListInstallmentsUseCase;
import com.pedrohenrique.pagcontrolback.usecases.PayInstallmentUseCase;
import com.pedrohenrique.pagcontrolback.usecases.UpdateInstallmentUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/installments")
public class InstallmentController {

    private final ListInstallmentsUseCase listInstallmentsUseCase;
    private final PayInstallmentUseCase payInstallmentUseCase;
    private final UpdateInstallmentUseCase updateInstallmentUseCase;

    public InstallmentController(
            ListInstallmentsUseCase listInstallmentsUseCase,
            PayInstallmentUseCase payInstallmentUseCase,
            UpdateInstallmentUseCase updateInstallmentUseCase
    ) {
        this.listInstallmentsUseCase = listInstallmentsUseCase;
        this.payInstallmentUseCase = payInstallmentUseCase;
        this.updateInstallmentUseCase = updateInstallmentUseCase;
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<InstallmentResponseDto>> getExpenses(
            @AuthenticationPrincipal UserPrincipal user,

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
        List<Installment> installments = listInstallmentsUseCase.execute(user.getId(), query);
        List<InstallmentResponseDto> response = installments.stream()
                .map(InstallmentMapper::fromDomain)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{installmentId}/pay")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> payInstallment(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID installmentId
    ) {
        payInstallmentUseCase.execute(user.getId(), installmentId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{installmentId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> updateInstallment(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID installmentId,
            @RequestBody InstallmentUpdateDto dto
    ) {

        UpdateInstallmentCommand command = new UpdateInstallmentCommand(
                dto.amount(),
                dto.dueDate(),
                dto.barcode(),
                user.getId(),
                installmentId
        );

        updateInstallmentUseCase.execute(command);
        return ResponseEntity.ok().build();
    }
}
