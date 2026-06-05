package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID>
{
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Supplier s WHERE s.cnpj.value = :cnpj AND s.user.id = :userId")
    boolean existsSupplierByCnpjAndUser_Id(String cnpj, UUID userId);

    List<Supplier> findAllByUser_Id(UUID userId);

    Optional<Supplier> findByIdAndUser_Id(UUID id, UUID userId);
}
