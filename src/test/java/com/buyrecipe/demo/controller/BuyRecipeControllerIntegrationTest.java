package com.buyrecipe.demo.controller;

import com.buyrecipe.demo.dto.CartResponse;
import com.buyrecipe.demo.dto.RecipeRequest;
import com.buyrecipe.demo.dto.RecipeResponse;
import com.buyrecipe.demo.model.*;
import com.buyrecipe.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class BuyRecipeControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeProductRepository recipeProductRepository;

    private Cart testCart1;
    private Cart testCart2;
    private Product testProduct1;
    private Product testProduct2;
    private Recipe testRecipe;

    @BeforeEach
    void setUp() {
        // Clean database and setup fresh test data
        cleanDatabase();
        setupTestData();
    }

    private void cleanDatabase() {
        // Clean up database to ensure fresh state
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        recipeProductRepository.deleteAll();
        recipeRepository.deleteAll();
        productRepository.deleteAll();
    }

    private void setupTestData() {
        // Create test products
        testProduct1 = productRepository.save(createProduct("Tomatoes", 399));
        testProduct2 = productRepository.save(createProduct("Onions", 250));

        // Create test carts
        testCart1 = cartRepository.save(createCart(649));
        testCart2 = cartRepository.save(createCart(798));

        // Create test cart items
        cartItemRepository.save(createCartItem(testCart1.getId(), testProduct1, 1));
        cartItemRepository.save(createCartItem(testCart1.getId(), testProduct2, 1));
        cartItemRepository.save(createCartItem(testCart2.getId(), testProduct1, 2));

        // Create test recipe
        testRecipe = recipeRepository.save(createRecipe("Tomato and Onion Salad"));

        // Create recipe products
        recipeProductRepository.save(createRecipeProduct(testRecipe, testProduct1, 2));
        recipeProductRepository.save(createRecipeProduct(testRecipe, testProduct2, 1));
    }

    private Product createProduct(String name, int priceInCents) {
        Product product = new Product();
        product.setName(name);
        product.setPriceInCents(priceInCents);
        return product;
    }

    private Cart createCart(int totalAmount) {
        Cart cart = new Cart();
        cart.setTotalAmount(totalAmount);
        return cart;
    }

    private CartItem createCartItem(Long cartId, Product product, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setCartId(cartId);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        return cartItem;
    }

    private Recipe createRecipe(String name) {
        Recipe recipe = new Recipe();
        recipe.setName(name);
        return recipe;
    }

    private RecipeProduct createRecipeProduct(Recipe recipe, Product product, int quantity) {
        RecipeProduct recipeProduct = new RecipeProduct();
        recipeProduct.setRecipe(recipe);
        recipeProduct.setProduct(product);
        recipeProduct.setQuantity(quantity);
        return recipeProduct;
    }

    @Test
    void getAllCarts_ShouldReturnAllCartsInDatabase() {
        String url = "http://localhost:" + port + "/buy-recipe/carts";
        
        ResponseEntity<Cart[]> response = restTemplate.getForEntity(url, Cart[].class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
        
        Cart[] carts = response.getBody();
        assertEquals(testCart1.getId(), carts[0].getId());
        assertEquals(649, carts[0].getTotalAmount());
        assertEquals(testCart2.getId(), carts[1].getId());
        assertEquals(798, carts[1].getTotalAmount());
    }

    @Test
    void getAllCarts_WhenNoCartsExist_ShouldReturnEmptyArray() {
        // Clean up all carts
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();

        String url = "http://localhost:" + port + "/buy-recipe/carts";
        
        ResponseEntity<Cart[]> response = restTemplate.getForEntity(url, Cart[].class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
    }

    @Test
    void getAllRecipes_ShouldReturnAllRecipesWithProducts() {
        String url = "http://localhost:" + port + "/buy-recipe/recipes";
        
        ResponseEntity<RecipeResponse[]> response = restTemplate.getForEntity(url, RecipeResponse[].class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
        
        RecipeResponse recipe = response.getBody()[0];
        assertEquals(testRecipe.getId(), recipe.getId());
        assertEquals("Tomato and Onion Salad", recipe.getName());
        assertEquals(2, recipe.getProducts().size());
        
        // Verify products in recipe
        assertEquals("Tomatoes", recipe.getProducts().get(0).getProductName());
        assertEquals(2, recipe.getProducts().get(0).getQuantity());
        assertEquals(399, recipe.getProducts().get(0).getPriceInCents());
        
        assertEquals("Onions", recipe.getProducts().get(1).getProductName());
        assertEquals(1, recipe.getProducts().get(1).getQuantity());
        assertEquals(250, recipe.getProducts().get(1).getPriceInCents());
    }

    @Test
    void getAllRecipes_WhenNoRecipesExist_ShouldReturnEmptyArray() {
        // Clean up all recipes
        recipeProductRepository.deleteAll();
        recipeRepository.deleteAll();

        String url = "http://localhost:" + port + "/buy-recipe/recipes";
        
        ResponseEntity<RecipeResponse[]> response = restTemplate.getForEntity(url, RecipeResponse[].class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
    }

    @Test
    void getCartById_WhenCartExists_ShouldReturnCartWithItems() {
        String url = "http://localhost:" + port + "/buy-recipe/carts/" + testCart1.getId();
        
        ResponseEntity<CartResponse> response = restTemplate.getForEntity(url, CartResponse.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        CartResponse cartResponse = response.getBody();
        assertEquals(testCart1.getId(), cartResponse.getId());
        assertEquals(649, cartResponse.getTotalAmount());
        assertEquals(2, cartResponse.getCartItems().size());
        
        // Verify cart items
        assertEquals("Tomatoes", cartResponse.getCartItems().get(0).getProductName());
        assertEquals(1, cartResponse.getCartItems().get(0).getQuantity());
        assertEquals(399, cartResponse.getCartItems().get(0).getPriceInCents());
        
        assertEquals("Onions", cartResponse.getCartItems().get(1).getProductName());
        assertEquals(1, cartResponse.getCartItems().get(1).getQuantity());
        assertEquals(250, cartResponse.getCartItems().get(1).getPriceInCents());
    }

    @Test
    void getCartById_WhenCartNotExists_ShouldReturn404() {
        Long nonExistentCartId = 999L;
        String url = "http://localhost:" + port + "/buy-recipe/carts/" + nonExistentCartId;
        
        ResponseEntity<CartResponse> response = restTemplate.getForEntity(url, CartResponse.class);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void addRecipeToCart_WhenValidRequest_ShouldAddRecipeSuccessfully() {
        // Create an empty cart for this test
        Cart emptyCart = new Cart();
        emptyCart.setTotalAmount(0);
        emptyCart = cartRepository.save(emptyCart);

        String url = "http://localhost:" + port + "/buy-recipe/carts/" + emptyCart.getId() + "/add_recipe";
        
        RecipeRequest request = new RecipeRequest();
        request.setRecipeId(testRecipe.getId());
        
        HttpEntity<RecipeRequest> requestEntity = new HttpEntity<>(request);
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Recipe added to cart successfully", response.getBody());
        
        // Verify cart was updated
        Cart updatedCart = cartRepository.findById(emptyCart.getId()).orElse(null);
        assertNotNull(updatedCart);
        // Recipe cost: 2 tomatoes (399*2) + 1 onion (250) = 1048
        assertEquals(1048, updatedCart.getTotalAmount());
        
        // Verify cart items were created
        List<CartItem> cartItems = cartItemRepository.findByCartId(emptyCart.getId());
        assertEquals(2, cartItems.size());
    }

    @Test
    void addRecipeToCart_WhenCartNotExists_ShouldReturn404() {
        Long nonExistentCartId = 999L;
        String url = "http://localhost:" + port + "/buy-recipe/carts/" + nonExistentCartId + "/add_recipe";
        
        RecipeRequest request = new RecipeRequest();
        request.setRecipeId(testRecipe.getId());
        
        HttpEntity<RecipeRequest> requestEntity = new HttpEntity<>(request);
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void addRecipeToCart_WhenRecipeNotExists_ShouldReturn400() {
        Long nonExistentRecipeId = 999L;
        String url = "http://localhost:" + port + "/buy-recipe/carts/" + testCart1.getId() + "/add_recipe";
        
        RecipeRequest request = new RecipeRequest();
        request.setRecipeId(nonExistentRecipeId);
        
        HttpEntity<RecipeRequest> requestEntity = new HttpEntity<>(request);
        ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Recipe not found", response.getBody());
    }

    @Test
    void removeRecipeFromCart_WhenValidRequest_ShouldRemoveRecipeSuccessfully() {
        // First add the recipe to testCart1
        String addUrl = "http://localhost:" + port + "/buy-recipe/carts/" + testCart1.getId() + "/add_recipe";
        RecipeRequest addRequest = new RecipeRequest();
        addRequest.setRecipeId(testRecipe.getId());
        HttpEntity<RecipeRequest> addRequestEntity = new HttpEntity<>(addRequest);
        restTemplate.postForEntity(addUrl, addRequestEntity, String.class);

        // Get the updated total amount
        Cart cartBeforeRemoval = cartRepository.findById(testCart1.getId()).orElse(null);
        assertNotNull(cartBeforeRemoval);
        int totalBeforeRemoval = cartBeforeRemoval.getTotalAmount();

        // Now remove the recipe
        String removeUrl = "http://localhost:" + port + "/buy-recipe/carts/" + testCart1.getId() + "/recipes/" + testRecipe.getId();
        
        ResponseEntity<String> response = restTemplate.exchange(
            removeUrl, 
            HttpMethod.DELETE, 
            null, 
            String.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Recipe removed from cart successfully", response.getBody());
        
        // Verify cart total was updated (should go back close to original or 0 due to quantity logic)
        Cart updatedCart = cartRepository.findById(testCart1.getId()).orElse(null);
        assertNotNull(updatedCart);
        assertTrue(updatedCart.getTotalAmount() < totalBeforeRemoval);
    }

    @Test
    void removeRecipeFromCart_WhenCartNotExists_ShouldReturn404() {
        Long nonExistentCartId = 999L;
        String url = "http://localhost:" + port + "/buy-recipe/carts/" + nonExistentCartId + "/recipes/" + testRecipe.getId();
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, 
            HttpMethod.DELETE, 
            null, 
            String.class
        );
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void removeRecipeFromCart_WhenRecipeNotExists_ShouldReturn400() {
        Long nonExistentRecipeId = 999L;
        String url = "http://localhost:" + port + "/buy-recipe/carts/" + testCart1.getId() + "/recipes/" + nonExistentRecipeId;
        
        ResponseEntity<String> response = restTemplate.exchange(
            url, 
            HttpMethod.DELETE, 
            null, 
            String.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Recipe not found", response.getBody());
    }
}