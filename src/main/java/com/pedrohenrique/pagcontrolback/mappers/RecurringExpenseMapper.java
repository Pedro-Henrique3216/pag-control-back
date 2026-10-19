package com.pedrohenrique.pagcontrolback.mappers;

import com.pedrohenrique.pagcontrolback.dtos.response.RecurringExpenseResponseDto;
import com.pedrohenrique.pagcontrolback.model.RecurringExpense;

public class RecurringExpenseMapper {

    public static RecurringExpenseResponseDto fromDomain(RecurringExpense recurringExpense){
        return new RecurringExpenseResponseDto(
                recurringExpense.getId(),
                recurringExpense.getDescription(),
                recurringExpense.getPaymentType(),
                recurringExpense.getSupplier() != null ? SupplierMapper.fromDomain(recurringExpense.getSupplier()) : null,
                recurringExpense.getStartDate(),
                recurringExpense.getAmount().value(),
                recurringExpense.getCategory() != null ? CategoryMapper.fromDomain(recurringExpense.getCategory()) : null,
                recurringExpense.getCreditCard() != null ? CreditCardMapper.toCreditCardSummaryResponseDto(recurringExpense.getCreditCard()) : null,
                recurringExpense.getRecurrenceDay(),
                recurringExpense.getRecurrenceType(),
                recurringExpense.getRecurrenceInterval(),
                recurringExpense.getRecurrenceEndDate()
        );
    }
}
