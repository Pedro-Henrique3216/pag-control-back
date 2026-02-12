package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.*;
import com.pedrohenrique.pagcontrolback.model.Installment;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PayInstallmentUseCase {

    private final UserRepository userRepository;

    private final InstallmentRepository installmentRepository;

    public PayInstallmentUseCase(UserRepository userRepository, InstallmentRepository installmentRepository) {
        this.userRepository = userRepository;
        this.installmentRepository = installmentRepository;
    }

    public void execute(UUID userId, UUID installmentId) {

        if(userId == null){
            throw new UserRequiredException("User ID is required");
        }

        if(installmentId == null){
            throw new InstallmentRequiredException("Installment ID is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Installment installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new InstallmentNotFoundException("Installment not found"));

        if (!installment.getExpense().getUser().getId().equals(user.getId())) {
            throw new InstallmentAccessDeniedException("Installment does not belong to the user");
        }

        installment.markAsPaid();
        installmentRepository.save(installment);
    }
}
