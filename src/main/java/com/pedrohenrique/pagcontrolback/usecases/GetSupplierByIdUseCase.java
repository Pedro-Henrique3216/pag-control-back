package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.exceptions.SupplierNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserNotFoundException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetSupplierByIdUseCase {

    private final UserRepository userRepository;

    private final SupplierRepository supplierRepository;

    public GetSupplierByIdUseCase(UserRepository userRepository, SupplierRepository supplierRepository) {
        this.userRepository = userRepository;
        this.supplierRepository = supplierRepository;
    }

    public Supplier execute(UUID userId, UUID supplierId) {

        if(userId == null){
            throw new UserRequiredException("User ID cannot be null");
        }

        if(supplierId == null){
            throw new SupplierRequiredException("Supplier ID cannot be null");
        }

        if(!userRepository.existsById(userId)){
            throw new UserNotFoundException("User not found");
        }

        return supplierRepository.findByIdAndUser_Id(supplierId, userId)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found"));
    }
}
