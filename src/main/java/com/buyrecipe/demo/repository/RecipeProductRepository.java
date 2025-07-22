package com.buyrecipe.demo.repository;

import com.buyrecipe.demo.model.RecipeProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeProductRepository extends JpaRepository<RecipeProduct, Long> {
}