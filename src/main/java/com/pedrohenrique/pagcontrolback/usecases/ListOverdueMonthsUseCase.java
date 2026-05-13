package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class ListOverdueMonthsUseCase {

    private final InstallmentRepository installmentRepository;

    public ListOverdueMonthsUseCase(InstallmentRepository installmentRepository) {
        this.installmentRepository = installmentRepository;
    }

    public List<YearMonth> execute(UUID userId) {
        if (userId == null) {
            throw new UserRequiredException("User ID cannot be null");
        }
        return installmentRepository.findOverdueMonthsByUser(userId)
                .stream()
                .map(ym -> YearMonth.of(Integer.parseInt(ym[0].toString()), Integer.parseInt(ym[1].toString())))
                .toList();
    }
}
