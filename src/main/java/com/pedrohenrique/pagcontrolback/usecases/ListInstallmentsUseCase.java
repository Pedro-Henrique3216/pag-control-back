package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.request.ListInstallmentQuery;
import com.pedrohenrique.pagcontrolback.exceptions.InvalidInstallmentFilterException;
import com.pedrohenrique.pagcontrolback.exceptions.UserIdRequiredException;
import com.pedrohenrique.pagcontrolback.model.Installment;
import com.pedrohenrique.pagcontrolback.model.TransactionType;
import com.pedrohenrique.pagcontrolback.repositories.CategoryRepository;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepositoryCustom;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListInstallmentsUseCase {

    private final InstallmentRepositoryCustom installmentRepositoryCustom;

    private final CategoryRepository categoryRepository;

    public ListInstallmentsUseCase(
            InstallmentRepositoryCustom installmentRepositoryCustom,
            CategoryRepository categoryRepository
    ) {
        this.installmentRepositoryCustom = installmentRepositoryCustom;
        this.categoryRepository = categoryRepository;
    }

    public List<Installment> execute(UUID userId, ListInstallmentQuery query) {
        if (userId == null) {
            throw new UserIdRequiredException("User id is required.");
        }

        if (query.status() != null && Boolean.TRUE.equals(query.overdue())) {
            throw new InvalidInstallmentFilterException("Cannot use status and overdue filters together.");
        }

        if (query.categoryId() != null) {
            categoryRepository
                    .findCategoryByIdAndUserId(query.categoryId(), userId)
                    .ifPresent(category -> {
                        if (category.getCategoryType() != TransactionType.EXPENSE) {
                            throw new InvalidInstallmentFilterException(
                                    "Category must be of type EXPENSE."
                            );
                        }
                    });

        }

        return installmentRepositoryCustom.search(query, userId);
    }

}
