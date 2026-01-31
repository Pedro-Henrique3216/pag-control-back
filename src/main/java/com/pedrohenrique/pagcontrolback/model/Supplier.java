package com.pedrohenrique.pagcontrolback.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(length = 15, unique = true)
    private String cnpj;
    @OneToMany(mappedBy = "supplier")
    private Set<Expense> expanses = new HashSet<>();
    @ManyToMany
    @JoinTable(
            name = "supplier_user",
            joinColumns = @JoinColumn(name = "supplier_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users = new HashSet<>();

    public Supplier() {}

    public Supplier(String name, String cnpj) {
        this.name = name;
        this.cnpj = cnpj;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCnpj() {
        return cnpj;
    }

    public Set<Expense> getExpanses() {
        return expanses;
    }

    public Set<User> getUsers() {
        return users;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Supplier supplier = (Supplier) o;
        return Objects.equals(id, supplier.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
