package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.events.InstallmentReminderEvent;
import com.pedrohenrique.pagcontrolback.dtos.response.InstallmentReminderData;
import com.pedrohenrique.pagcontrolback.model.Installment;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SendInstallmentReminderUseCase {

    private final InstallmentRepository installmentRepository;
    private final ApplicationEventPublisher publisher;

    public SendInstallmentReminderUseCase(
            InstallmentRepository installmentRepository,
            ApplicationEventPublisher publisher
    ) {
        this.installmentRepository = installmentRepository;
        this.publisher = publisher;
    }


    public void execute(){

        LocalDate today = LocalDate.now();
        LocalDate limitDate = today.plusDays(7);

        List<Installment> installments = installmentRepository.findPendingInstallmentsUntil(
                limitDate
        );

        Map<User, List<Installment>> groupedByUser = installments.stream()
                .collect(Collectors.groupingBy(installment -> installment.getExpense().getUser()));

        groupedByUser.entrySet()
                .stream()
                .map(entry -> transform(entry, today))
                .forEach(publisher::publishEvent);
    }

    private InstallmentReminderEvent transform(Map.Entry<User, List<Installment>> entry, LocalDate today) {

        List<InstallmentReminderData> overdue =  entry.getValue()
                .stream()
                .filter(installment -> installment.getDueDate().isBefore(today))
                .map(this::toReminderData)
                .toList();

        List<InstallmentReminderData> upcoming =  entry.getValue()
                .stream()
                .filter(installment -> !installment.getDueDate().isBefore(today))
                .map(this::toReminderData)
                .toList();

        return new InstallmentReminderEvent(
                entry.getKey().getName(),
                entry.getKey().getEmail().value(),
                overdue,
                upcoming
        );
    }

    private InstallmentReminderData toReminderData(Installment installment) {
        return new InstallmentReminderData(
                installment.getExpense().getDescription(),
                installment.getAmount().value(),
                installment.getDueDate(),
                installment.getBarcode()
        );
    }
}
