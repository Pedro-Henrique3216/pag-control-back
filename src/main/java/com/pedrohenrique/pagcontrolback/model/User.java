package com.pedrohenrique.pagcontrolback.model;

import com.pedrohenrique.pagcontrolback.exceptions.UserDomainException;
import jakarta.persistence.*;

import java.time.LocalDateTime;
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
    @Column(length = 150, name = "fantasy_name")
    private String fantasyName;
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, length = 15)
    private String phone;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "person_type")
    private PersonType personType;
    @OneToMany(mappedBy = "user")
    private Set<Category> categories =  new HashSet<>();
    @OneToMany(mappedBy = "user")
    private Set<Supplier> suppliers = new HashSet<>();
    @Column(nullable = false, name = "created_at")
    private LocalDateTime  createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public User(
            String name,
            String fantasyName,
            String email,
            String password,
            String phone,
            PersonType personType
    ) {
        validateUser(name, fantasyName, email, password, phone, personType);
        this.name = name;
        this.fantasyName = fantasyName;
        this.email = email.toLowerCase().trim();
        this.password = password;
        this.phone = normalizePhone(phone);
        this.personType = personType;
        this.createdAt = LocalDateTime.now();
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

    private void validateUser(String name, String fantasyName, String email, String password, String phone, PersonType personType) {
        if (name == null || name.trim().isEmpty()) {
            throw new UserDomainException("Name cannot be null or empty");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new UserDomainException("Email cannot be null or empty");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new UserDomainException("Email format is invalid");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new UserDomainException("Password cannot be null or empty");
        }


        if (password.length() < 8) {
            throw new UserDomainException("Password must be at least 8 characters");
        }

        if (phone == null || phone.trim().isEmpty()) {
            throw new UserDomainException("Phone cannot be null or empty");
        }

        phone = normalizePhone(phone);
        if (phone.length() != 11) {
            throw new UserDomainException("Phone must be 11 digits");
        }

        validatePersonType(personType, fantasyName);
    }

    private String normalizePhone(String phone) {
        return phone.replaceAll("[^\\d]", "");
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

    public Set<Supplier> getSuppliers() {
        return suppliers;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setPassword(String password) {
        this.password = password;
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
