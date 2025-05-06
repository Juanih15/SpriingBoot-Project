package com.moneymapper.budgettracker.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Expense {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Category category;

    @DecimalMin("0.01")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal amount;

    @SuppressWarnings("unused")
    private LocalDate date;

    protected Expense() {
    } // for JPA

    public Expense(Category c, BigDecimal amt, LocalDate date) {
        this.category = c;
        this.amount = amt;
        this.date = date;
    }

}