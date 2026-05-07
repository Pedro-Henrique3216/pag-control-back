package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.InstallmentAccessDeniedException;
import com.pedrohenrique.pagcontrolback.exceptions.InstallmentNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.InstallmentRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.model.Installment;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PayInstallmentUseCase {

    private final InstallmentRepository installmentRepository;

    public PayInstallmentUseCase(InstallmentRepository installmentRepository) {
        this.installmentRepository = installmentRepository;
    }

    public void execute(UUID userId, UUID installmentId) {

        if(userId == null){
            throw new UserRequiredException("User ID is required");
        }

        if(installmentId == null){
            throw new InstallmentRequiredException("Installment ID is required");
        }

        Installment installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new InstallmentNotFoundException("Installment not found"));

        if (!installment.getExpense().getUser().getId().equals(userId)) {
            throw new InstallmentAccessDeniedException("Installment does not belong to the user");
        }

        installment.markAsPaid();
        installmentRepository.save(installment);
    }
}
