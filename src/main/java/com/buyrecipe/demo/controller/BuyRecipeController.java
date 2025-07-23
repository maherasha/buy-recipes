package com.buyrecipe.demo.controller;

import com.buyrecipe.demo.dto.*;
import com.buyrecipe.demo.model.*;
import com.buyrecipe.demo.service.CartService;
import com.buyrecipe.demo.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/buy-recipe")
@Tag(name = "Buy Recipe API", description = "E-commerce API for managing carts and recipes")
public class BuyRecipeController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private RecipeService recipeService;
    
    @GetMapping("/carts")
    @Operation(summary = "Get all carts", description = "Retrieves a list of all carts with basic information (id, totalAmount)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all carts",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = Cart.class)))
    })
    public List<Cart> getAllCarts() {
        return cartService.getAllCarts();
    }
    
    @GetMapping("/recipes")
    @Operation(summary = "Get all recipes", description = "Retrieves a list of all available recipes with their products")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all recipes",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = RecipeResponse.class)))
    })
    public List<RecipeResponse> getAllRecipes() {
        return recipeService.getAllRecipes();
    }
    
    @GetMapping("/carts/{id}")
    @Operation(summary = "Get cart by ID", description = "Retrieves detailed information about a specific cart including all cart items")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart found successfully",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = CartResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cart not found", content = @Content)
    })
    public ResponseEntity<CartResponse> getCartById(
            @Parameter(description = "ID of the cart to retrieve", required = true)
            @PathVariable Long id) {
        Optional<CartResponse> cart = cartService.getCartById(id);
        return cart.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/carts/{cartId}/add_recipe")
    @Operation(summary = "Add recipe to cart", description = "Adds all products from a recipe to the specified cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recipe successfully added to cart",
                    content = @Content(mediaType = "text/plain", 
                                     schema = @Schema(type = "string", example = "Recipe added to cart successfully"))),
        @ApiResponse(responseCode = "400", description = "Recipe not found",
                    content = @Content(mediaType = "text/plain", 
                                     schema = @Schema(type = "string", example = "Recipe not found"))),
        @ApiResponse(responseCode = "404", description = "Cart not found", content = @Content)
    })
    public ResponseEntity<String> addRecipeToCart(
            @Parameter(description = "ID of the cart to add recipe to", required = true)
            @PathVariable Long cartId, 
            @Parameter(description = "Recipe request containing recipe ID", required = true)
            @RequestBody RecipeRequest request) {
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
    @Operation(summary = "Remove recipe from cart", description = "Removes all products associated with a recipe from the specified cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recipe successfully removed from cart",
                    content = @Content(mediaType = "text/plain", 
                                     schema = @Schema(type = "string", example = "Recipe removed from cart successfully"))),
        @ApiResponse(responseCode = "400", description = "Recipe not found",
                    content = @Content(mediaType = "text/plain", 
                                     schema = @Schema(type = "string", example = "Recipe not found"))),
        @ApiResponse(responseCode = "404", description = "Cart not found", content = @Content)
    })
    public ResponseEntity<String> removeRecipeFromCart(
            @Parameter(description = "ID of the cart to remove recipe from", required = true)
            @PathVariable Long cartId, 
            @Parameter(description = "ID of the recipe to remove", required = true)
            @PathVariable Long recipeId) {
        String result = cartService.removeRecipeFromCart(cartId, recipeId);
        
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (result.equals("Recipe not found")) {
            return ResponseEntity.badRequest().body(result);
        }
        
        return ResponseEntity.ok(result);
    }
    
}
