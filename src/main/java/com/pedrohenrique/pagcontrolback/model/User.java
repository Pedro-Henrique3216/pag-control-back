package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.exceptions.UserDomainException;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(length = 150)
    private String fantasyName;
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, length = 15)
    private String phone;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PersonType personType;
    @OneToMany(mappedBy = "user")
    private Set<Expense> expenses = new HashSet<>();
    @OneToMany(mappedBy = "user", orphanRemoval = true)
    private Set<Supplier> suppliers = new HashSet<>();

    public User(
            String name,
            String fantasyName,
            String email,
            String password,
            String phone,
            PersonType personType
    ) {
        validatePersonType(personType, fantasyName);
        this.name = name;
        this.fantasyName = fantasyName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.personType = personType;
    }

    public User() {
    }

    private void validatePersonType(PersonType personType, String fantasyName) {
        if (personType == null) {
            throw new UserDomainException("Person type cannot be null");
        }

        if (personType == PersonType.PF && fantasyName != null && !fantasyName.isBlank()) {
            throw new UserDomainException("Fantasy name must be null or blank for individuals (PF)");
        }

        if (personType == PersonType.PJ && (fantasyName == null || fantasyName.isBlank()))
            throw new UserDomainException("Fantasy name cannot be null or blank for companies (PJ)"); {
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFantasyName() {
        return fantasyName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhone() {
        return phone;
    }

    public PersonType getPersonType() {
        return personType;
    }

    public Set<Expense> getExpenses() {
        return expenses;
    }

    public Set<Supplier> getSuppliers() {
        return suppliers;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
