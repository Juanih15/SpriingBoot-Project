package com.moneymapper.budgettracker.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    // null ⇒ this is a top-level node (“Living Expenses”)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    // Convenience back-reference
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Category> children = new HashSet<>();

    // null ⇒ a system-wide starter category
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    // JPA only
    protected Category() {
    }

    public Category(String name, Category parent, User owner) {
        this.name = name;
        this.parent = parent;
        this.owner = owner;
    }

    public void forEach(Object object) {
        throw new UnsupportedOperationException("Unimplemented method 'forEach'");
    }

    public void setName(String newName) {
        throw new UnsupportedOperationException("Unimplemented method 'setName'");
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

    public Set<Category> getChildren() {
        return children;
    }

}
