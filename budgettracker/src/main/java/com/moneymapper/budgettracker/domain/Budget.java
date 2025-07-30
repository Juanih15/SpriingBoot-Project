package com.moneymapper.budgettracker.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(indexes = @Index(columnList = "owner_id,startDate"))
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(name = "budget_limit")
    private BigDecimal budgetLimit;
    private LocalDate startDate;
    private LocalDate endDate;

    // linkd
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User owner;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses = new ArrayList<>();

    protected Budget() {
    }

    // compatibility ctor – matches the one in the test
    public Budget(String name, BigDecimal limit,
            LocalDate start, LocalDate end) {
        this.name = name;
        this.budgetLimit = limit;
        this.startDate = start;
        this.endDate = end;
    }

    /* getters + helpers used by the test */
    public void setOwner(User u) {
        this.owner = u;
    }

    public User getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBudgetLimit() {
        return budgetLimit;
    }

    public void setBudgetLimit(BigDecimal budgetLimit) {
        this.budgetLimit = budgetLimit;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
        expense.setBudget(this);
    }
}
