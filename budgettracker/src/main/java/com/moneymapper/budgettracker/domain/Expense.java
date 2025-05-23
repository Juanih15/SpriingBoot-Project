package com.moneymapper.budgettracker.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private Budget budget;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    /** Optional free-text note. */
    private String memo;

    protected Expense() {
    } // JPA only

    public Expense(Category category,
            Budget budget,
            BigDecimal amount,
            LocalDate date) {
        this.category = category;
        this.budget = budget;
        this.amount = amount;
        this.date = date;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
