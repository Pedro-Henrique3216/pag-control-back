package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.UpdateSupplierCommand;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateSupplierUseCase {

    private final SupplierRepository supplierRepository;

    public UpdateSupplierUseCase(
            SupplierRepository supplierRepository
    ) {
        this.supplierRepository = supplierRepository;
    }

    @Transactional
    public Supplier updateSupplier(UUID supplierId, UUID userId, UpdateSupplierCommand command) {
        if (supplierId == null) {
            throw new IllegalArgumentException("supplier id cannot be null");
        }

        if (userId == null) {
            throw new IllegalArgumentException("user id cannot be null");
        }

        if (command == null) {
            throw new IllegalArgumentException("update supplier command cannot be null");
        }

        Supplier supplier = supplierRepository.findByIdAndUser_Id(supplierId, userId)
                .orElseThrow(() -> new SupplierNotFoundException("supplier not found"));

        supplier.update(
                command.name(),
                command.cnpj()
        );

        supplierRepository.save(supplier);

        return supplier;
    }

}
