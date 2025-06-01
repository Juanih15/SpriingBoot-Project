package com.moneymapper.budgettracker.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expense")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Category category;

    /** Optional: link each expense to a budget period */
    @ManyToOne(fetch = FetchType.LAZY)
    private Budget budget;

    private BigDecimal amount;
    private LocalDate date;

    /** Optional free-text memo / note */
    private String memo;

    protected Expense() {
    } // JPA

    // full 4-arg ctor (category, budget, amount, date)
    public Expense(Category category,
            Budget budget,
            BigDecimal amount,
            LocalDate date) {
        this.category = category;
        this.budget = budget;
        this.amount = amount;
        this.date = date;
    }

    // 3-arg convenience ctor (category, amount, date)
    public Expense(Category category,
            BigDecimal amount,
            LocalDate date) {
        this(category, null, amount, date);
    }

    public Long getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public Budget getBudget() {
        return budget;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getMemo() {
        return memo;
    }

    public void setCategory(Category c) {
        this.category = c;
    }

    public void setBudget(Budget b) {
        this.budget = b;
    }

    public void setAmount(BigDecimal a) {
        this.amount = a;
    }

    public void setDate(LocalDate d) {
        this.date = d;
    }

    public void setMemo(String m) {
        this.memo = m;
    }
}
