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
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id")
    private Budget budget;

    @ManyToOne
    @JoinColumn(name = "user_id") // column name in expense table referencing user.id
    private User user;

    public void setBudget(Budget b) {
        this.budget = b;
    }

    public Budget getBudget() {
        return budget;
    } // used in the test

    private BigDecimal amount;
    private LocalDate date;

    // Optional free-text memo
    private String memo;

    @Column(length = 120)
    private String description;

    protected Expense() {
    } // JPA

    public Expense(Category category,
            String description,
            BigDecimal amount,
            LocalDate date) {
        this(category, (Budget) null, amount, date);
        this.description = description;
    }

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
        this(category, (Budget) null, amount, date);
    }

    public Long getId() {
        return id;
    }

    public Category getCategory() {
        return category;
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

    public void setAmount(BigDecimal a) {
        this.amount = a;
    }

    public void setDate(LocalDate d) {
        this.date = d;
    }

    public void setMemo(String m) {
        this.memo = m;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getExpenseDate() {return date;}

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setDescription(String description) {this.description = description;
    }

    public void setExpenseDate(LocalDate localDate) {this.date = localDate;
    }
}
