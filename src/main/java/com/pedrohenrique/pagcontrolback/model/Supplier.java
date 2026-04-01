package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.exceptions.InvalidSupplierCnpjException;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierNameRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import com.pedrohenrique.pagcontrolback.utils.ValidateCnpj;
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
    private String cnpj;
    @OneToMany(mappedBy = "supplier")
    private Set<Expense> expanses = new HashSet<>();
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Supplier() {}


    public Supplier(String name, String cnpj, User user) {
        validateName(name);
        this.name = name;
        setCnpj(cnpj);
        setUser(user);
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new SupplierNameRequiredException("Supplier name cannot be null or empty.");
        }
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        if (user == null) {
            throw new UserRequiredException("Supplier must have an user.");
        }
        this.user = user;
    }

    public void setCnpj(String cnpj) {
        if (cnpj == null || cnpj.isBlank()) {
            return;
        }

        cnpj = cnpj.replaceAll("\\D", "");

        if (!ValidateCnpj.isValidCnpj(cnpj)) {
            throw new InvalidSupplierCnpjException("Invalid CNPJ format.");
        }


        this.cnpj = cnpj;
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
