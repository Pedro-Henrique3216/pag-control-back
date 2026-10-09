package com.pedrohenrique.pagcontrolback.services;

import java.time.LocalDate;

public class CreditCardDueDateCalculator {

    public static LocalDate calculateDueDate(
            LocalDate purchaseDate,
            int closingDay,
            int dueDay,
            int installmentNumber
    ){
        if(purchaseDate.getDayOfMonth() <= closingDay){
            return purchaseDate.plusMonths(installmentNumber - 1).withDayOfMonth(dueDay);
        }

        return purchaseDate.plusMonths(installmentNumber).withDayOfMonth(dueDay);
    }
}
