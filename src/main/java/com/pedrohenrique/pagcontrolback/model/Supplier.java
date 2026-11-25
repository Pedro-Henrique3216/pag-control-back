package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.ValueObjects.Cnpj;
import com.pedrohenrique.pagcontrolback.exceptions.SupplierNameRequiredException;
import com.pedrohenrique.pagcontrolback.exceptions.UserRequiredException;
import jakarta.persistence.*;

import java.time.LocalDateTime;
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
    @Embedded
    @AttributeOverride(
            name = "value",
            column = @Column(name = "cnpj", unique = true, length = 14)
    )
    private Cnpj cnpj;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    private boolean active;

    public Supplier() {}


    public Supplier(String name, String cnpj, User user) {
        validateName(name);
        this.name = name;
        setCnpj(cnpj);
        setUser(user);
        this.createdAt = LocalDateTime.now();
        this.active = true;
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

    public Cnpj getCnpj() {
        return cnpj;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setUser(User user) {
        if (user == null) {
            throw new UserRequiredException("User cannot be null");
        }

        this.user = user;
    }

    public void setCnpj(String cnpj) {
        if (cnpj == null || cnpj.isBlank()) {
            this.cnpj = null;
            return;
        }

        this.cnpj = new Cnpj(cnpj);
    }

    public void update(String name, String cnpj) {
        validateName(name);
        this.name = name;
        setCnpj(cnpj);
        this.updatedAt = LocalDateTime.now();
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
