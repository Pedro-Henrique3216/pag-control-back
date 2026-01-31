package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.model.Installment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InstallmentRepository extends JpaRepository<Installment, UUID>
{
}
