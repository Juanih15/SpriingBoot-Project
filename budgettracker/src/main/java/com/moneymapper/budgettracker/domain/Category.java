package com.moneymapper.budgettracker.domain;

import com.moneymapper.budgettracker.dto.UserReadDTO;
import jakarta.persistence.*;
import java.util.Objects;

/** A user-defined (or system) expense category that may be nested. */
@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** null ⇒ this is a root category. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Category parent;

    /** null ⇒ shared across all users (system default). */
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    protected Category() { // only jpa
    }

    public Category(String name) { // constructor for test file
        this.name = name;
    }

    public Category(String name, Category parent, User owner) {
        this.name = name;
        this.parent = parent;
        this.owner = owner;
    }


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Category getParent() {
        return parent;
    }

    public User getOwner() {
        return owner;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Category c && Objects.equals(id, c.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Category[id=%d,%s]".formatted(id, name);
    }
}
