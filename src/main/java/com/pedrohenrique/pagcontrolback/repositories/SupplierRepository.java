package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID>
{
    boolean existsSupplierByCnpjAndUser_Id(String cnpj, UUID userId);

    List<Supplier> findAllByUser_Id(UUID userId);

    Optional<Supplier> findByIdAndUser_Id(UUID id, UUID userId);
}
