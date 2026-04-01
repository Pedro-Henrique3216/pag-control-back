package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.UpdateInstallmentCommand;
import com.pedrohenrique.pagcontrolback.exceptions.*;
import com.pedrohenrique.pagcontrolback.model.Installment;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UpdateInstallmentUseCase {

    private final UserRepository userRepository;
    private final InstallmentRepository installmentRepository;

    public UpdateInstallmentUseCase(UserRepository userRepository, InstallmentRepository installmentRepository) {
        this.userRepository = userRepository;
        this.installmentRepository = installmentRepository;
    }

    public void execute(UpdateInstallmentCommand command) {

        if(command.userId() == null){
            throw new UserRequiredException("User ID is required");
        }

        if(command.installmentId() == null){
            throw new InstallmentRequiredException("Installment ID is required");
        }

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + command.userId()));

        Installment existingInstallment = installmentRepository.findById(command.installmentId())
                .orElseThrow(() -> new InstallmentNotFoundException("Installment not found with ID: " + command.installmentId()));

        if(!existingInstallment.getExpense().getUser().equals(user)){
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
