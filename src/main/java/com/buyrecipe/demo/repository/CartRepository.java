package com.buyrecipe.demo.repository;

import com.buyrecipe.demo.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    /**
     * Fetch all carts basic data only (id, totalAmount) - optimized for getAllCarts endpoint
     * This avoids fetching unnecessary cart items data when we only need basic cart info
     */
    @Query("SELECT c FROM Cart c")
    List<Cart> findAllCartsBasicData();
    
    /**
     * Fetch all carts with their cart items and products in a single query to avoid N+1 problem
     * Used when cart items data is needed (for detailed cart responses)
     */
    @Query("SELECT DISTINCT c FROM Cart c " +
           "LEFT JOIN FETCH c.cartItems ci " +
           "LEFT JOIN FETCH ci.product")
    List<Cart> findAllWithItemsAndProducts();
    
    /**
     * Fetch a specific cart with its cart items and products in a single query
     */
    @Query("SELECT c FROM Cart c " +
           "LEFT JOIN FETCH c.cartItems ci " +
           "LEFT JOIN FETCH ci.product " +
           "WHERE c.id = :id")
    Optional<Cart> findByIdWithItemsAndProducts(@Param("id") Long id);
}