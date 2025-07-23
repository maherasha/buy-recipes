package com.buyrecipe.demo.repository;

import com.buyrecipe.demo.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    
    /**
     * Fetch all recipes with their recipe products and products in a single query to avoid N+1 problem
     */
    @Query("SELECT DISTINCT r FROM Recipe r " +
           "LEFT JOIN FETCH r.recipeProducts rp " +
           "LEFT JOIN FETCH rp.product")
    List<Recipe> findAllWithProductsAndDetails();
    
    /**
     * Fetch a specific recipe with its recipe products and products in a single query
     */
    @Query("SELECT r FROM Recipe r " +
           "LEFT JOIN FETCH r.recipeProducts rp " +
           "LEFT JOIN FETCH rp.product " +
           "WHERE r.id = :id")
    Optional<Recipe> findByIdWithProductsAndDetails(@Param("id") Long id);
}