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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
    private CartItem testCartItem1;
    private CartItem testCartItem2;
    private Recipe testRecipe;
    private RecipeProduct testRecipeProduct1;
    private RecipeProduct testRecipeProduct2;

    @BeforeEach
    void setUp() {
        // Clean up database to ensure fresh state
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        recipeProductRepository.deleteAll();
        recipeRepository.deleteAll();
        productRepository.deleteAll();

        // Create test products
        testProduct1 = new Product();
        testProduct1.setName("Tomatoes");
        testProduct1.setPriceInCents(399);
        testProduct1 = productRepository.save(testProduct1);

        testProduct2 = new Product();
        testProduct2.setName("Onions");
        testProduct2.setPriceInCents(250);
        testProduct2 = productRepository.save(testProduct2);

        // Create test carts
        testCart1 = new Cart();
        testCart1.setTotalAmount(649); // 399 + 250
        testCart1 = cartRepository.save(testCart1);

        testCart2 = new Cart();
        testCart2.setTotalAmount(798); // 399 * 2
        testCart2 = cartRepository.save(testCart2);

        // Create test cart items
        testCartItem1 = new CartItem();
        testCartItem1.setCartId(testCart1.getId());
        testCartItem1.setProduct(testProduct1);
        testCartItem1.setQuantity(1);
        cartItemRepository.save(testCartItem1);

        testCartItem2 = new CartItem();
        testCartItem2.setCartId(testCart1.getId());
        testCartItem2.setProduct(testProduct2);
        testCartItem2.setQuantity(1);
        cartItemRepository.save(testCartItem2);

        // Cart 2 has 2 tomatoes
        CartItem cartItem3 = new CartItem();
        cartItem3.setCartId(testCart2.getId());
        cartItem3.setProduct(testProduct1);
        cartItem3.setQuantity(2);
        cartItemRepository.save(cartItem3);

        // Create test recipe
        testRecipe = new Recipe();
        testRecipe.setName("Tomato and Onion Salad");
        testRecipe = recipeRepository.save(testRecipe);

        // Create recipe products
        testRecipeProduct1 = new RecipeProduct();
        testRecipeProduct1.setRecipe(testRecipe);
        testRecipeProduct1.setProduct(testProduct1);
        testRecipeProduct1.setQuantity(2);
        recipeProductRepository.save(testRecipeProduct1);

        testRecipeProduct2 = new RecipeProduct();
        testRecipeProduct2.setRecipe(testRecipe);
        testRecipeProduct2.setProduct(testProduct2);
        testRecipeProduct2.setQuantity(1);
        recipeProductRepository.save(testRecipeProduct2);
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