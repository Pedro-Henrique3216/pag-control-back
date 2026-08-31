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
        SELECT COALESCE(SUM(i.amount.value), 0)
                        FROM Installment i
                        WHERE i.expense.user.id = :userId
                        AND i.status = 'PAID'
                        AND i.dueDate BETWEEN :startDate AND :endDate
    """)
    BigDecimal sumPaidByUserIdAndDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);

    @Query("""
        SELECT new com.pedrohenrique.pagcontrolback.dtos.response.CategorySummaryDto(
            COALESCE(c.name, 'outros'),
            SUM(i.amount.value)
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

    @Query("""
        SELECT COALESCE(SUM(i.amount.value), 0)
        FROM Installment i
        WHERE i.expense.user.id = :userId
        AND i.status = 'UNPAID'
        AND i.dueDate < CURRENT_DATE
     """)
    BigDecimal sumOverdueByUser(UUID userId);

    @Query("""
        SELECT COUNT(i)
        FROM Installment i
        WHERE i.expense.user.id = :userId
        AND i.status = 'UNPAID'
        AND i.dueDate < CURRENT_DATE
    """)
    Integer countOverdueByUser(UUID userId);

    @Query("""
        SELECT COALESCE(SUM(i.amount.value), 0)
        FROM Installment i
        WHERE i.expense.user.id = :userId
        AND i.status = 'UNPAID'
        AND i.dueDate BETWEEN CURRENT_DATE AND :futureDate
    """)
    BigDecimal sumUpcomingByUser(UUID userId, LocalDate futureDate);

    @Query("""
        SELECT COUNT(i)
        FROM Installment i
        WHERE i.expense.user.id = :userId
        AND i.status = 'UNPAID'
        AND i.dueDate BETWEEN CURRENT_DATE AND :futureDate
    """)
    Integer countUpcomingByUser(UUID userId, LocalDate futureDate);

    @Query("""
        SELECT DISTINCT
           YEAR(i.dueDate),
           MONTH(i.dueDate)
        FROM Installment i
        WHERE i.expense.user.id = :userId
        AND i.status = 'UNPAID'
        AND i.dueDate < CURRENT_DATE
        ORDER BY YEAR(i.dueDate), MONTH(i.dueDate)
   """)
    List<Object[]> findOverdueMonthsByUser(UUID userId);

    @Query("""
        SELECT i
        FROM Installment i
        WHERE i.status = 'UNPAID'
        AND i.dueDate <= :limitDate
        ORDER BY i.dueDate ASC
    """)
    List<Installment> findPendingInstallmentsUntil(LocalDate limitDate);
}
