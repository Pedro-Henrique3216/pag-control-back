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
    @Column(nullable = false, length = 13)
    private String phone;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PersonType personType;
    @OneToMany(mappedBy = "user")
    private Set<Expense> expenses = new HashSet<>();
    @ManyToMany(mappedBy = "users")
    private Set<Supplier> suppliers = new HashSet<>();

    public User(
            String name,
            String fantasyName,
            String email,
            String password,
            String phone,
            PersonType personType
    ) {
        validateUser(name, fantasyName, email, password, phone);
        this.name = name;
        this.fantasyName = fantasyName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.personType = personType;
    }

    public User() {
    }

    public void validateUser(
            String name,
            String fantasyName,
            String username,
            String password,
            String phone
    ){
        if (name == null || name.isBlank() || name.length() > 150) {
            throw new UserDomainException("Name must be between 1 and 150 characters.");
        }
        if (fantasyName != null && fantasyName.length() > 150) {
            throw new UserDomainException("Fantasy Name must be up to 150 characters.");
        }
        if (username == null || username.isBlank() || username.length() > 100) {
            throw new UserDomainException("Username must be between 1 and 100 characters.");
        }
        if (password == null || password.isBlank()) {
            throw new UserDomainException("Password cannot be null or blank.");
        }
        if (password.length() < 8) {
            throw new UserDomainException("Password must be at least 8 characters long.");
        }
        if (phone == null || phone.isBlank() || phone.length() > 13) {
            throw new UserDomainException("Phone must be between 1 and 13 characters.");
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
