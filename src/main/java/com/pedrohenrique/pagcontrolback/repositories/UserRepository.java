package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsUserByEmail(String email);
}
