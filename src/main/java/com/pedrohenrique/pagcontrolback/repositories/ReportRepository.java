package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.dtos.response.MonthSummaryDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class ReportRepository implements IReportRepository {

    @PersistenceContext
    private EntityManager em;

    public List<MonthSummaryDto> findMonthlySummaryByUserId(UUID userId, LocalDate startDate, LocalDate endDate){

        Query query = em.createNativeQuery(
                """
               
                        SELECT
                       year_value,
                       month_value,
                       COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount END), 0) AS income,
                       COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount END), 0) AS expense
                   FROM (
                       SELECT
                           EXTRACT(YEAR FROM i.date) AS year_value,
                           EXTRACT(MONTH FROM i.date) AS month_value,
                           i.amount AS amount,
                           'INCOME' AS type
                       FROM incomes i
                       WHERE i.user_id = :userId
                       AND i.date BETWEEN :startDate AND :endDate
                
                       UNION ALL
                
                       SELECT
                           EXTRACT(YEAR FROM i.due_date) AS year_value,
                           EXTRACT(MONTH FROM i.due_date) AS month_value,
                           i.amount AS amount,
                           'EXPENSE' AS type
                       FROM installments i
                       JOIN expenses e ON i.expense_id = e.id
                       WHERE e.user_id = :userId
                       AND i.due_date BETWEEN :startDate AND :endDate
                       AND i.status = 'PAID'
                   ) AS combined
                   GROUP BY year_value, month_value
                   ORDER BY year_value, month_value
                """
        );

        query.setParameter("userId", userId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        List<Object[]> results = query.getResultList();
        List<MonthSummaryDto> monthSummaryDtos = new ArrayList<>();
        for (Object[] result : results) {
            int year = ((Number) result[0]).intValue();
            int month = ((Number) result[1]).intValue();

            Number incomeNumber = (Number) result[2];
            Number expenseNumber = (Number) result[3];

            monthSummaryDtos.add(new MonthSummaryDto(
                    YearMonth.of(year, month),
                    incomeNumber != null ? BigDecimal.valueOf(incomeNumber.doubleValue()) : BigDecimal.ZERO,
                    expenseNumber != null ? BigDecimal.valueOf(expenseNumber.doubleValue()) : BigDecimal.ZERO
            ));
        }

        return monthSummaryDtos;
    }
}
