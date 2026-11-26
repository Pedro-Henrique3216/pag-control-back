package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.SupplierNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeactivateSupplierUseCase {

    private final SupplierRepository supplierRepository;

    public DeactivateSupplierUseCase(
            SupplierRepository supplierRepository
    ) {
        this.supplierRepository = supplierRepository;
    }

    public void deactivateSupplier(UUID supplierId, UUID userId) {
        if (supplierId == null) {
            throw new IllegalArgumentException("supplier id cannot be null");
        }

        if (userId == null) {
            throw new IllegalArgumentException("user id cannot be null");
        }

        Supplier supplier = supplierRepository.findByIdAndUser_Id(supplierId, userId)
                .orElseThrow(() -> new SupplierNotFoundException("supplier not found"));

        supplier.deactivate();

        supplierRepository.save(supplier);
    }
}
