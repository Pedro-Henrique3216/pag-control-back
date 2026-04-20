package com.pedrohenrique.pagcontrolback.helpers;

import com.pedrohenrique.pagcontrolback.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseCleaner {

    @Autowired
    private InstallmentRepository installmentRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void clearDatabase() {

        installmentRepository.deleteAll();

        expenseRepository.deleteAll();
        incomeRepository.deleteAll();

        categoryRepository.deleteAll();
        supplierRepository.deleteAll();

        userRepository.deleteAll();
    }
}