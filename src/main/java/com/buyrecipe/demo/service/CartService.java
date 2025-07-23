package com.buyrecipe.demo.service;

import com.buyrecipe.demo.dto.CartItemResponse;
import com.buyrecipe.demo.dto.CartResponse;
import com.buyrecipe.demo.model.*;
import com.buyrecipe.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private RecipeRepository recipeRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    public List<Cart> getAllCarts() {
        return cartRepository.findAllCartsBasicData();
    }
    
    public Optional<CartResponse> getCartById(Long id) {
        Optional<Cart> cartOpt = cartRepository.findByIdWithItemsAndProducts(id);
        return cartOpt.map(this::convertToCartResponseOptimized);
    }

    private CartResponse convertToCartResponseOptimized(Cart cart) {
        List<CartItemResponse> cartItemResponses = cart.getCartItems().stream()
            .map(item -> new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getPriceInCents(),
                item.getQuantity()
            ))
            .collect(Collectors.toList());
        
        return new CartResponse(cart.getId(), cart.getTotalAmount(), cartItemResponses);
    }
    
    public String addRecipeToCart(Long cartId, Long recipeId) {
        Optional<Cart> cartOpt = cartRepository.findById(cartId);
        Optional<Recipe> recipeOpt = recipeRepository.findByIdWithProductsAndDetails(recipeId);
        
        if (cartOpt.isEmpty()) {
            return null; // Cart not found
        }
        
        if (recipeOpt.isEmpty()) {
            return "Recipe not found";
        }
        
        Cart cart = cartOpt.get();
        Recipe recipe = recipeOpt.get();
        List<CartItem> existingItems = cartItemRepository.findByCartId(cartId);
        
        // Add all products from the recipe to the cart using quantity
        int totalAdded = 0;
        for (RecipeProduct recipeProduct : recipe.getRecipeProducts()) {
            Product product = recipeProduct.getProduct();
            Integer quantityToAdd = recipeProduct.getQuantity();
            
            // Check if product already exists in cart
            Optional<CartItem> existingItem = existingItems.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();
            
            if (existingItem.isPresent()) {
                // Update existing item quantity
                CartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + quantityToAdd);
                cartItemRepository.save(item);
            } else {
                // Create new cart item
                CartItem cartItem = new CartItem();
                cartItem.setCartId(cart.getId());
                cartItem.setProduct(product);
                cartItem.setQuantity(quantityToAdd);
                cartItemRepository.save(cartItem);
                existingItems.add(cartItem);
            }
            
            totalAdded += product.getPriceInCents() * quantityToAdd;
        }
        
        // Update cart total amount
        cart.setTotalAmount(cart.getTotalAmount() + totalAdded);
        cartRepository.save(cart);
        
        return "Recipe added to cart successfully";
    }
    
    public String removeRecipeFromCart(Long cartId, Long recipeId) {
        Optional<Cart> cartOpt = cartRepository.findById(cartId);
        // Use optimized query to fetch recipe with its products in a single query
        Optional<Recipe> recipeOpt = recipeRepository.findByIdWithProductsAndDetails(recipeId);
        
        if (cartOpt.isEmpty()) {
            return null; // Cart not found
        }
        
        if (recipeOpt.isEmpty()) {
            return "Recipe not found";
        }
        
        Cart cart = cartOpt.get();
        Recipe recipe = recipeOpt.get();
        List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);
        
        // Remove products from the cart using quantity logic
        int totalRemoved = 0;
        for (RecipeProduct recipeProduct : recipe.getRecipeProducts()) {
            Product product = recipeProduct.getProduct();
            Integer quantityToRemove = recipeProduct.getQuantity();
            
            // Find the cart item for this product
            Optional<CartItem> cartItemOpt = cartItems.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();
            
            if (cartItemOpt.isPresent()) {
                CartItem cartItem = cartItemOpt.get();
                int currentQuantity = cartItem.getQuantity();
                int actualRemoved = Math.min(quantityToRemove, currentQuantity);
                
                totalRemoved += product.getPriceInCents() * actualRemoved;
                
                if (actualRemoved >= currentQuantity) {
                    // Remove the entire cart item
                    cartItemRepository.delete(cartItem);
                    cartItems.remove(cartItem);
                } else {
                    // Reduce the quantity
                    cartItem.setQuantity(currentQuantity - actualRemoved);
                    cartItemRepository.save(cartItem);
                }
            }
        }
        
        // Update cart total amount
        cart.setTotalAmount(Math.max(0, cart.getTotalAmount() - totalRemoved));
        cartRepository.save(cart);
        
        return "Recipe removed from cart successfully";
    }
}