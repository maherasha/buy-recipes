package com.buyrecipe.demo.service;

import com.buyrecipe.demo.model.*;
import com.buyrecipe.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private RecipeRepository recipeRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }
    
    public Optional<Cart> getCartById(Long id) {
        return cartRepository.findById(id);
    }
    
    public String addRecipeToCart(Long cartId, Long recipeId) {
        Optional<Cart> cartOpt = cartRepository.findById(cartId);
        Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);
        
        if (cartOpt.isEmpty()) {
            return null; // Cart not found
        }
        
        if (recipeOpt.isEmpty()) {
            return "Recipe not found";
        }
        
        Cart cart = cartOpt.get();
        Recipe recipe = recipeOpt.get();
        
        // Add all products from the recipe to the cart
        int totalAdded = 0;
        for (RecipeProduct recipeProduct : recipe.getRecipeProducts()) {
            for (int i = 0; i < recipeProduct.getQuantity(); i++) {
                CartItem cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setProduct(recipeProduct.getProduct());
                cartItemRepository.save(cartItem);
                totalAdded += recipeProduct.getProduct().getPriceInCents();
            }
        }
        
        // Update cart total amount
        cart.setTotalAmount(cart.getTotalAmount() + totalAdded);
        cartRepository.save(cart);
        
        return "Recipe added to cart successfully";
    }
    
    public String removeRecipeFromCart(Long cartId, Long recipeId) {
        Optional<Cart> cartOpt = cartRepository.findById(cartId);
        Optional<Recipe> recipeOpt = recipeRepository.findById(recipeId);
        
        if (cartOpt.isEmpty()) {
            return null; // Cart not found
        }
        
        if (recipeOpt.isEmpty()) {
            return "Recipe not found";
        }
        
        Cart cart = cartOpt.get();
        Recipe recipe = recipeOpt.get();
        
        // Remove products from the cart that belong to this recipe
        int totalRemoved = 0;
        List<CartItem> cartItems = cart.getCartItems();
        
        for (RecipeProduct recipeProduct : recipe.getRecipeProducts()) {
            Product productToRemove = recipeProduct.getProduct();
            int quantityToRemove = recipeProduct.getQuantity();
            
            for (int i = 0; i < quantityToRemove && i < cartItems.size(); i++) {
                CartItem itemToRemove = cartItems.stream()
                    .filter(item -> item.getProduct().getId().equals(productToRemove.getId()))
                    .findFirst()
                    .orElse(null);
                
                if (itemToRemove != null) {
                    totalRemoved += itemToRemove.getProduct().getPriceInCents();
                    cartItemRepository.delete(itemToRemove);
                    cartItems.remove(itemToRemove);
                }
            }
        }
        
        // Update cart total amount
        cart.setTotalAmount(Math.max(0, cart.getTotalAmount() - totalRemoved));
        cartRepository.save(cart);
        
        return "Recipe removed from cart successfully";
    }
}