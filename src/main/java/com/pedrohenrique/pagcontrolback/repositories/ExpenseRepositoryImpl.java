package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.dtos.request.ListExpensesQuery;
import com.pedrohenrique.pagcontrolback.model.Expense;
import com.pedrohenrique.pagcontrolback.model.Installment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class ExpenseRepositoryImpl implements ExpenseRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

     @Override
     public List<Expense> search(ListExpensesQuery query, UUID userId) {

         CriteriaBuilder cb = em.getCriteriaBuilder();

         CriteriaQuery<Expense> cq = cb.createQuery(Expense.class);

         Root<Expense> expense = cq.from(Expense.class);

         Join<Expense, Installment> installment =
                 expense.join("installments", JoinType.LEFT);

         List<Predicate> predicates = new ArrayList<>();

         predicates.add(
                 cb.equal(expense.get("user").get("id"), userId)
         );

         if (query.supplierId() != null) {
             predicates.add(
                     cb.equal(expense.get("supplier").get("id"), query.supplierId())
             );
         }

         if (query.month() != null) {
             predicates.add(
                     cb.equal(
                             cb.function(
                                     "to_char",
                                     String.class,
                                     expense.get("expenseDate"),
                                     cb.literal("YYYY-MM")
                             ),
                             query.month().toString()
                     )
             );
         }

         if (query.invoiceNumber() != null && !query.invoiceNumber().isBlank()) {
             predicates.add(
                     cb.like(
                             cb.lower(expense.get("invoiceNumber")),
                             "%" + query.invoiceNumber().toLowerCase() + "%"
                     )
             );
         }

         cq.select(expense).distinct(true)
                 .where(predicates.toArray(new Predicate[0]));

         return em.createQuery(cq).getResultList();
     }
}
