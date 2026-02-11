package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.dtos.request.ListInstallmentQuery;
import com.pedrohenrique.pagcontrolback.model.Installment;

import java.util.List;
import java.util.UUID;

public interface InstallmentRepositoryCustom {

    List<Installment> search(ListInstallmentQuery query, UUID userId);
}
