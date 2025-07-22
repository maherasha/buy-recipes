package com.buyrecipe.demo.service;

import com.buyrecipe.demo.dto.RecipeProductResponse;
import com.buyrecipe.demo.dto.RecipeResponse;
import com.buyrecipe.demo.model.Recipe;
import com.buyrecipe.demo.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecipeService {
    
    @Autowired
    private RecipeRepository recipeRepository;
    
    public List<RecipeResponse> getAllRecipes() {
        return recipeRepository.findAll().stream()
            .map(this::convertToRecipeResponse)
            .collect(Collectors.toList());
    }
    
    private RecipeResponse convertToRecipeResponse(Recipe recipe) {
        List<RecipeProductResponse> products = recipe.getRecipeProducts().stream()
            .map(rp -> new RecipeProductResponse(
                rp.getProduct().getId(),
                rp.getProduct().getName(),
                rp.getProduct().getPriceInCents(),
                rp.getQuantity()
            ))
            .collect(Collectors.toList());
        
        return new RecipeResponse(recipe.getId(), recipe.getName(), products);
    }
}