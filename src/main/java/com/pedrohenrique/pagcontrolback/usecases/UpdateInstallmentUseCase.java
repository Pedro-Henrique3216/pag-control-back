package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.UpdateInstallmentCommand;
import com.pedrohenrique.pagcontrolback.exceptions.InstallmentAccessDeniedException;
import com.pedrohenrique.pagcontrolback.exceptions.InstallmentNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.InstallmentRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.model.Installment;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepository;
import org.springframework.stereotype.Service;

@Service
public class UpdateInstallmentUseCase {

    private final InstallmentRepository installmentRepository;

    public UpdateInstallmentUseCase(InstallmentRepository installmentRepository) {
        this.installmentRepository = installmentRepository;
    }

    public void execute(UpdateInstallmentCommand command) {

        if(command.userId() == null){
            throw new UserRequiredException("User ID is required");
        }

        if(command.installmentId() == null){
            throw new InstallmentRequiredException("Installment ID is required");
        }

        Installment existingInstallment = installmentRepository.findById(command.installmentId())
                .orElseThrow(() -> new InstallmentNotFoundException("Installment not found with ID: " + command.installmentId()));

        if(!existingInstallment.getExpense().getUser().getId().equals(command.userId())){
            throw new InstallmentAccessDeniedException("User does not have permission to update installment");
        }

        existingInstallment.updateInstallment(
                command.amount(),
                command.dueDate(),
                command.barcode()
        );

        installmentRepository.save(existingInstallment);
    }
}
