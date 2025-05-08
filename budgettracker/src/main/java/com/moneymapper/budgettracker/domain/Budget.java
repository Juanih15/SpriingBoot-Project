package com.moneymapper.budgettracker.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "budgets", uniqueConstraints = @UniqueConstraint(name = "uk_budget_owner_category", columnNames = {
        "owner_id", "category_id" }))
public class Budget {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User owner;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Category category;

    @DecimalMin("0.01")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal limitAmount;

    protected Budget() {
    }

    public Budget(User owner, Category category, BigDecimal limitAmount) {
        this.owner = owner;
        this.category = category;
        this.limitAmount = limitAmount;
    }

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public Category getCategory() {
        return category;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setLimitAmount(BigDecimal limit) {
        this.limitAmount = limit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Budget other))
            return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Budget{id=%d, owner=%s, category=%s, limit=%s}"
                .formatted(id,
                        owner == null ? "null" : owner.getId(),
                        category == null ? "null" : category.getId(),
                        limitAmount);
    }
}
