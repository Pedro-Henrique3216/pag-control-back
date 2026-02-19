package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.SupplierRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.response.SupplierResponseDto;
import com.pedrohenrique.pagcontrolback.mappers.SupplierMapper;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.model.User;
import com.pedrohenrique.pagcontrolback.usecases.CreateSupplierUseCase;
import com.pedrohenrique.pagcontrolback.usecases.GetSupplierByIdUseCase;
import com.pedrohenrique.pagcontrolback.usecases.ListSuppliersUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    private final CreateSupplierUseCase createSupplierUseCase;

    private final ListSuppliersUseCase listSuppliersUseCase;

    private final GetSupplierByIdUseCase getSupplierByIdUseCase;

    public SupplierController(
            CreateSupplierUseCase createSupplierUseCase,
            ListSuppliersUseCase listSuppliersUseCase,
            GetSupplierByIdUseCase getSupplierByIdUseCase
    ) {
        this.createSupplierUseCase = createSupplierUseCase;
        this.listSuppliersUseCase = listSuppliersUseCase;
        this.getSupplierByIdUseCase = getSupplierByIdUseCase;

    }

    @PostMapping
    public ResponseEntity<SupplierResponseDto> save(
            @Valid @RequestBody SupplierRequestDto dto,
            UriComponentsBuilder uriComponentsBuilder,
            @AuthenticationPrincipal User user
    ) {
        Supplier supplier = SupplierMapper.toDomain(dto);
        Supplier savedSupplier = createSupplierUseCase.execute(supplier, user.getId());
        URI uri = uriComponentsBuilder.path("/suppliers/{id}").buildAndExpand(savedSupplier.getId()).toUri();
        SupplierResponseDto responseDto = SupplierMapper.fromDomain(savedSupplier);
        return ResponseEntity.created(uri).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponseDto>> listSuppliers(
            @AuthenticationPrincipal User user
    ) {
        List<Supplier> suppliers = listSuppliersUseCase.execute(user.getId());
        List<SupplierResponseDto> responseDtos = suppliers.stream()
                .map(SupplierMapper::fromDomain)
                .toList();
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/{supplierId}")
    public ResponseEntity<SupplierResponseDto> getSupplierById(
            @AuthenticationPrincipal User user,
            @PathVariable UUID supplierId
    ) {
        Supplier supplier = getSupplierByIdUseCase.execute(user.getId(), supplierId);
        SupplierResponseDto responseDto = SupplierMapper.fromDomain(supplier);
        return ResponseEntity.ok(responseDto);
    }

}
