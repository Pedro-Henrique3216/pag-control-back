package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.dtos.response.CategorySummaryDto;
import com.pedrohenrique.pagcontrolback.model.Installment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InstallmentRepository extends JpaRepository<Installment, UUID>
{
    @Query("""
        SELECT COALESCE(SUM(i.amount), 0)
                        FROM Installment i
                        WHERE i.expense.user.id = :userId
                        AND i.status = 'PAID'
                        AND i.dueDate BETWEEN :startDate AND :endDate
    """)
    BigDecimal sumPaidByUserIdAndDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);

    @Query("""
        SELECT new com.pedrohenrique.pagcontrolback.dtos.response.CategorySummaryDto(
            COALESCE(c.name, 'outros'),
            SUM(i.amount)
        )
        FROM Installment i
        JOIN i.expense e
        LEFT JOIN e.category c
        WHERE e.user.id = :userId
        AND i.status = 'PAID'
        AND i.dueDate BETWEEN :start AND :end
        GROUP BY COALESCE(c.name, 'outros')
    """)
    List<CategorySummaryDto> sumByCategory(UUID userId, LocalDate start, LocalDate end);
}
