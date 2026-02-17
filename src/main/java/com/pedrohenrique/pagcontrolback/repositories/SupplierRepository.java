package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID>
{
    boolean existsSupplierByCnpjAndUser_Id(String cnpj, UUID userId);

    List<Supplier> findAllByUser_Id(UUID userId);
}
