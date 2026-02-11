package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.request.ListInstallmentQuery;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.UserIdRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Installment;
import com.pedrohenrique.pagcontrolback.repositories.InstallmentRepositoryCustom;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListInstallmentsUseCase {

    private final UserRepository userRepository;

    private final InstallmentRepositoryCustom installmentRepositoryCustom;

    private final SupplierRepository supplierRepository;

    public ListInstallmentsUseCase(
            UserRepository userRepository,
            InstallmentRepositoryCustom installmentRepositoryCustom,
            SupplierRepository supplierRepository
    ) {
        this.userRepository = userRepository;
        this.installmentRepositoryCustom = installmentRepositoryCustom;
        this.supplierRepository = supplierRepository;
    }

    public List<Installment> execute(UUID userId, ListInstallmentQuery query) {
        if (userId == null) {
            throw new UserIdRequiredException("User id is required.");
        }

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found.");
        }
        if(query.supplierId() != null && !supplierRepository.existsById(query.supplierId())){
            throw new SupplierNotFoundException("Supplier not found");
        }

        return installmentRepositoryCustom.search(query, userId);
    }


}
