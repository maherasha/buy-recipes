package com.buyrecipe.demo.service;

import com.buyrecipe.demo.dto.RecipeProductResponse;
import com.buyrecipe.demo.dto.RecipeResponse;
import com.buyrecipe.demo.model.Product;
import com.buyrecipe.demo.model.Recipe;
import com.buyrecipe.demo.model.RecipeProduct;
import com.buyrecipe.demo.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeService recipeService;

    private Recipe testRecipe1;
    private Recipe testRecipe2;
    private Product testProduct1;
    private Product testProduct2;
    private Product testProduct3;
    private RecipeProduct testRecipeProduct1;
    private RecipeProduct testRecipeProduct2;
    private RecipeProduct testRecipeProduct3;

    @BeforeEach
    void setUp() {
        // Create test products
        testProduct1 = new Product();
        testProduct1.setId(1L);
        testProduct1.setName("Tomatoes");
        testProduct1.setPriceInCents(399);

        testProduct2 = new Product();
        testProduct2.setId(2L);
        testProduct2.setName("Onions");
        testProduct2.setPriceInCents(250);

        testProduct3 = new Product();
        testProduct3.setId(3L);
        testProduct3.setName("Chicken Breast");
        testProduct3.setPriceInCents(1299);

        // Create test recipe products for recipe 1
        testRecipeProduct1 = new RecipeProduct();
        testRecipeProduct1.setId(1L);
        testRecipeProduct1.setProduct(testProduct1);
        testRecipeProduct1.setQuantity(2);

        testRecipeProduct2 = new RecipeProduct();
        testRecipeProduct2.setId(2L);
        testRecipeProduct2.setProduct(testProduct2);
        testRecipeProduct2.setQuantity(1);

        // Create test recipe product for recipe 2
        testRecipeProduct3 = new RecipeProduct();
        testRecipeProduct3.setId(3L);
        testRecipeProduct3.setProduct(testProduct3);
        testRecipeProduct3.setQuantity(1);

        // Create test recipe 1 (multi-product recipe)
        testRecipe1 = new Recipe();
        testRecipe1.setId(1L);
        testRecipe1.setName("Pasta with Tomato Sauce");
        testRecipe1.setRecipeProducts(Arrays.asList(testRecipeProduct1, testRecipeProduct2));

        // Create test recipe 2 (single-product recipe)
        testRecipe2 = new Recipe();
        testRecipe2.setId(2L);
        testRecipe2.setName("Grilled Chicken");
        testRecipe2.setRecipeProducts(Arrays.asList(testRecipeProduct3));
    }

    @Test
    void getAllRecipes_WhenRecipesExist_ShouldReturnRecipeResponseList() {
        // Given
        List<Recipe> recipes = Arrays.asList(testRecipe1, testRecipe2);
        when(recipeRepository.findAllWithProductsAndDetails()).thenReturn(recipes);

        // When
        List<RecipeResponse> result = recipeService.getAllRecipes();

        // Then
        assertEquals(2, result.size());
        
        // Verify first recipe
        RecipeResponse recipe1Response = result.get(0);
        assertEquals(1L, recipe1Response.getId());
        assertEquals("Pasta with Tomato Sauce", recipe1Response.getName());
        assertEquals(2, recipe1Response.getProducts().size());
        
        RecipeProductResponse product1 = recipe1Response.getProducts().get(0);
        assertEquals(1L, product1.getProductId());
        assertEquals("Tomatoes", product1.getProductName());
        assertEquals(399, product1.getPriceInCents());
        assertEquals(2, product1.getQuantity());
        
        RecipeProductResponse product2 = recipe1Response.getProducts().get(1);
        assertEquals(2L, product2.getProductId());
        assertEquals("Onions", product2.getProductName());
        assertEquals(250, product2.getPriceInCents());
        assertEquals(1, product2.getQuantity());

        // Verify second recipe
        RecipeResponse recipe2Response = result.get(1);
        assertEquals(2L, recipe2Response.getId());
        assertEquals("Grilled Chicken", recipe2Response.getName());
        assertEquals(1, recipe2Response.getProducts().size());
        
        RecipeProductResponse product3 = recipe2Response.getProducts().get(0);
        assertEquals(3L, product3.getProductId());
        assertEquals("Chicken Breast", product3.getProductName());
        assertEquals(1299, product3.getPriceInCents());
        assertEquals(1, product3.getQuantity());

        verify(recipeRepository).findAllWithProductsAndDetails();
    }

    @Test
    void getAllRecipes_WhenNoRecipesExist_ShouldReturnEmptyList() {
        // Given
        when(recipeRepository.findAllWithProductsAndDetails()).thenReturn(Collections.emptyList());

        // When
        List<RecipeResponse> result = recipeService.getAllRecipes();

        // Then
        assertTrue(result.isEmpty());
        verify(recipeRepository).findAllWithProductsAndDetails();
    }

    @Test
    void getAllRecipes_WhenRecipeHasNoProducts_ShouldReturnRecipeWithEmptyProductList() {
        // Given
        Recipe emptyRecipe = new Recipe();
        emptyRecipe.setId(3L);
        emptyRecipe.setName("Empty Recipe");
        emptyRecipe.setRecipeProducts(Collections.emptyList());
        
        when(recipeRepository.findAllWithProductsAndDetails()).thenReturn(Arrays.asList(emptyRecipe));

        // When
        List<RecipeResponse> result = recipeService.getAllRecipes();

        // Then
        assertEquals(1, result.size());
        RecipeResponse recipeResponse = result.get(0);
        assertEquals(3L, recipeResponse.getId());
        assertEquals("Empty Recipe", recipeResponse.getName());
        assertTrue(recipeResponse.getProducts().isEmpty());

        verify(recipeRepository).findAllWithProductsAndDetails();
    }

    @Test
    void getAllRecipes_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Given
        RuntimeException expectedException = new RuntimeException("Database error");
        when(recipeRepository.findAllWithProductsAndDetails()).thenThrow(expectedException);

        // When & Then
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            recipeService.getAllRecipes();
        });

        assertEquals("Database error", thrown.getMessage());
        verify(recipeRepository).findAllWithProductsAndDetails();
    }

    @Test
    void convertToRecipeResponse_ShouldCorrectlyMapAllFields() {
        // This test verifies the private method through the public getAllRecipes method
        // Given
        when(recipeRepository.findAllWithProductsAndDetails()).thenReturn(Arrays.asList(testRecipe1));

        // When
        List<RecipeResponse> result = recipeService.getAllRecipes();

        // Then
        assertEquals(1, result.size());
        RecipeResponse response = result.get(0);
        
        // Verify recipe mapping
        assertEquals(testRecipe1.getId(), response.getId());
        assertEquals(testRecipe1.getName(), response.getName());
        assertEquals(testRecipe1.getRecipeProducts().size(), response.getProducts().size());
        
        // Verify product mapping
        for (int i = 0; i < response.getProducts().size(); i++) {
            RecipeProductResponse productResponse = response.getProducts().get(i);
            RecipeProduct originalRecipeProduct = testRecipe1.getRecipeProducts().get(i);
            
            assertEquals(originalRecipeProduct.getProduct().getId(), productResponse.getProductId());
            assertEquals(originalRecipeProduct.getProduct().getName(), productResponse.getProductName());
            assertEquals(originalRecipeProduct.getProduct().getPriceInCents(), productResponse.getPriceInCents());
            assertEquals(originalRecipeProduct.getQuantity(), productResponse.getQuantity());
        }
    }

    @Test
    void getAllRecipes_WhenMultipleRecipesWithVaryingProductCounts_ShouldReturnCorrectStructure() {
        // Given
        Recipe singleProductRecipe = new Recipe();
        singleProductRecipe.setId(4L);
        singleProductRecipe.setName("Simple Recipe");
        
        RecipeProduct singleRecipeProduct = new RecipeProduct();
        singleRecipeProduct.setProduct(testProduct1);
        singleRecipeProduct.setQuantity(3);
        singleProductRecipe.setRecipeProducts(Arrays.asList(singleRecipeProduct));

        when(recipeRepository.findAllWithProductsAndDetails()).thenReturn(Arrays.asList(testRecipe1, singleProductRecipe));

        // When
        List<RecipeResponse> result = recipeService.getAllRecipes();

        // Then
        assertEquals(2, result.size());
        
        // First recipe should have 2 products
        assertEquals(2, result.get(0).getProducts().size());
        
        // Second recipe should have 1 product
        assertEquals(1, result.get(1).getProducts().size());
        assertEquals("Simple Recipe", result.get(1).getName());
        assertEquals(3, result.get(1).getProducts().get(0).getQuantity());

        verify(recipeRepository).findAllWithProductsAndDetails();
    }
}