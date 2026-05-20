package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.request.ListInstallmentQuery;
import com.pedrohenrique.pagcontrolback.exceptions.InvalidInstallmentFilterException;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.UserIdRequiredException;
import com.pedrohenrique.pagcontrolback.model.Installment;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepositoryCustom;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListInstallmentsUseCase {

    private final InstallmentRepositoryCustom installmentRepositoryCustom;

    private final SupplierRepository supplierRepository;

    public ListInstallmentsUseCase(
            InstallmentRepositoryCustom installmentRepositoryCustom,
            SupplierRepository supplierRepository
    ) {
        this.installmentRepositoryCustom = installmentRepositoryCustom;
        this.supplierRepository = supplierRepository;
    }

    public List<Installment> execute(UUID userId, ListInstallmentQuery query) {
        if (userId == null) {
            throw new UserIdRequiredException("User id is required.");
        }

        if(query.supplierId() != null && !supplierRepository.existsById(query.supplierId())){
            throw new SupplierNotFoundException("Supplier not found");
        }

        if (query.status() != null && Boolean.TRUE.equals(query.overdue())) {
            throw new InvalidInstallmentFilterException("Cannot use status and overdue filters together.");
        }

        return installmentRepositoryCustom.search(query, userId);
    }


}
