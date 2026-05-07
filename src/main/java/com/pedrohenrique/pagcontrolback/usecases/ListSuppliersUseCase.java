package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListSuppliersUseCase {

    private final SupplierRepository supplierRepository;

    public ListSuppliersUseCase(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    public List<Supplier> execute(UUID userId) {

        if (userId == null) {
            throw new UserRequiredException("User ID is required");
        }

        return supplierRepository.findAllByUser_Id(userId);
    }
}
