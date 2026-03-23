package com.pedrohenrique.pagcontrolback.repositories;

import com.pedrohenrique.pagcontrolback.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsCategoryByNameAndUser_Id(String name, UUID userId);

    Optional<Category> findCategoryByIdAndUserId(UUID id, UUID userId);
}
