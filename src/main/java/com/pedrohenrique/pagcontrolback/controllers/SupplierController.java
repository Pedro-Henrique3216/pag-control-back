package com.pedrohenrique.pagcontrolback.controllers;

import com.pedrohenrique.pagcontrolback.dtos.request.SupplierRequestDto;
import com.pedrohenrique.pagcontrolback.dtos.response.SupplierResponseDto;
import com.pedrohenrique.pagcontrolback.mappers.SupplierMapper;
import com.pedrohenrique.pagcontrolback.model.Supplier;
import com.pedrohenrique.pagcontrolback.usecases.CreateSupplierUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    private final CreateSupplierUseCase createSupplierUseCase;

    public SupplierController(CreateSupplierUseCase createSupplierUseCase) {
        this.createSupplierUseCase = createSupplierUseCase;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<SupplierResponseDto> save(@Valid @RequestBody SupplierRequestDto dto, UriComponentsBuilder uriComponentsBuilder, @PathVariable UUID userId) {
        Supplier supplier = SupplierMapper.toDomain(dto);
        Supplier savedSupplier = createSupplierUseCase.execute(supplier, userId);
        URI uri = uriComponentsBuilder.path("/suppliers/{id}").buildAndExpand(savedSupplier.getId()).toUri();
        SupplierResponseDto responseDto = SupplierMapper.fromDomain(savedSupplier);
        return ResponseEntity.created(uri).body(responseDto);
    }
}
