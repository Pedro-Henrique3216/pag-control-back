package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.SupplierAlreadyExistsWithCnpjException;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserIdRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateSupplierUseCase {

    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;

    public CreateSupplierUseCase(SupplierRepository supplierRepository, UserRepository userRepository) {
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
    }

    public Supplier execute(Supplier supplier, UUID userId) {

        if (supplier == null) {
            throw new SupplierRequiredException("Supplier cannot be null.");
        }

        if (userId == null) {
            throw new UserIdRequiredException("User ID cannot be null.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        if(supplier.getCnpj() != null && supplierRepository.existsSupplierByCnpjAndUser_Id(supplier.getCnpj(), userId)) {
            throw new SupplierAlreadyExistsWithCnpjException("Supplier with this CNPJ already exists for this user.");
        }

        supplier.setUser(user);
        return supplierRepository.save(supplier);
    }
}
