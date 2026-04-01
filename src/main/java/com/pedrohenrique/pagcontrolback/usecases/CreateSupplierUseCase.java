package com.pedrohenrique.pagcontrolback.usecases;

import com.pedrohenrique.pagcontrolback.dtos.command.CreateSupplierCommand;
import com.pedrohenrique.pagcontrolback.exceptions.CreateSupplierCommandRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierAlreadyExistsWithCnpjException;
import com.pedrohenrique.pagcontrolback.exceptions.UserIdRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserNotFoundException;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.repositories.SupplierRepository;
import com.pedrohenrique.pagcontrolback.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateSupplierUseCase {

    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;

    public CreateSupplierUseCase(SupplierRepository supplierRepository, UserRepository userRepository) {
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
    }

    public Supplier execute(CreateSupplierCommand command) {

        if (command == null) {
            throw new CreateSupplierCommandRequiredException("Create supplier command cannot be null.");
        }

        if (command.userId() == null) {
            throw new UserIdRequiredException("User ID cannot be null.");
        }

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + command.userId()));

        Supplier supplier = new Supplier(
                command.name(),
                command.cnpj(),
                user
        );

        if(supplier.getCnpj() != null && supplierRepository.existsSupplierByCnpjAndUser_Id(supplier.getCnpj(), command.userId())) {
            throw new SupplierAlreadyExistsWithCnpjException("Supplier with this CNPJ already exists for this user.");
        }

        return supplierRepository.save(supplier);
    }
}
