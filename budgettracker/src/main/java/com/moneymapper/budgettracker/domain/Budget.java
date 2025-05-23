package com.moneymapper.budgettracker.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User owner;

    private String name;

    /** Planned amount for the whole period. */
    @Column(precision = 12, scale = 2)
    private BigDecimal limit;

    private LocalDate startDate;
    private LocalDate endDate;

    protected Budget() {
    }

    public Budget(User owner, String name,
            BigDecimal limit, LocalDate startDate, LocalDate endDate) {
        this.owner = owner;
        this.name = name;
        this.limit = limit;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void applyExpense(BigDecimal amount) {
        if (limit != null)
            limit = limit.subtract(amount);
    }

    /* getters … */

    @Override
    public boolean equals(Object o) {
        return o instanceof Budget b && Objects.equals(id, b.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
