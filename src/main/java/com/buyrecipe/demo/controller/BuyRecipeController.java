package com.buyrecipe.demo.controller;

import com.buyrecipe.demo.dto.*;
import com.buyrecipe.demo.model.*;
import com.buyrecipe.demo.service.CartService;
import com.buyrecipe.demo.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/buy-recipe")
public class BuyRecipeController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private RecipeService recipeService;
    
    @GetMapping("/carts")
    public List<Cart> getAllCarts() {
        return cartService.getAllCarts();
    }
    
    @GetMapping("/recipes")
    public List<RecipeResponse> getAllRecipes() {
        return recipeService.getAllRecipes();
    }
    
    @GetMapping("/carts/{id}")
    public ResponseEntity<Cart> getCartById(@PathVariable Long id) {
        Optional<Cart> cart = cartService.getCartById(id);
        return cart.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/carts/{cartId}/add_recipe")
    public ResponseEntity<String> addRecipeToCart(@PathVariable Long cartId, @RequestBody RecipeRequest request) {
        String result = cartService.addRecipeToCart(cartId, request.getRecipeId());
        
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (result.equals("Recipe not found")) {
            return ResponseEntity.badRequest().body(result);
        }
        
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/carts/{cartId}/recipes/{recipeId}")
    public ResponseEntity<String> removeRecipeFromCart(@PathVariable Long cartId, @PathVariable Long recipeId) {
        String result = cartService.removeRecipeFromCart(cartId, recipeId);
        
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (result.equals("Recipe not found")) {
            return ResponseEntity.badRequest().body(result);
        }
        
        return ResponseEntity.ok(result);
    }
    
    public static class RecipeRequest {
        private Long recipeId;
        
        public Long getRecipeId() {
            return recipeId;
        }
        
        public void setRecipeId(Long recipeId) {
            this.recipeId = recipeId;
        }
    }
}
