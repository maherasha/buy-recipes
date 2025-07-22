package com.buyrecipe.demo.controller;

import com.buyrecipe.demo.model.Cart;
import com.buyrecipe.demo.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/buy-recipe")
public class BuyRecipeController {
    
    @Autowired
    private CartRepository cartRepository;
    
    @GetMapping("/get-all-carts")
    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }
}
