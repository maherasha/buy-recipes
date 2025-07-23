package com.buyrecipe.demo.service;

import com.buyrecipe.demo.dto.CartItemResponse;
import com.buyrecipe.demo.dto.CartResponse;
import com.buyrecipe.demo.model.*;
import com.buyrecipe.demo.repository.CartItemRepository;
import com.buyrecipe.demo.repository.CartRepository;
import com.buyrecipe.demo.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartService cartService;

    private Cart testCart;
    private Recipe testRecipe;
    private Product testProduct1;
    private Product testProduct2;
    private CartItem testCartItem1;
    private CartItem testCartItem2;
    private RecipeProduct testRecipeProduct1;
    private RecipeProduct testRecipeProduct2;

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

        // Create test cart
        testCart = new Cart();
        testCart.setId(1L);
        testCart.setTotalAmount(1000);

        // Create test cart items
        testCartItem1 = new CartItem();
        testCartItem1.setId(1L);
        testCartItem1.setCartId(1L);
        testCartItem1.setProduct(testProduct1);
        testCartItem1.setQuantity(2);

        testCartItem2 = new CartItem();
        testCartItem2.setId(2L);
        testCartItem2.setCartId(1L);
        testCartItem2.setProduct(testProduct2);
        testCartItem2.setQuantity(1);

        // Create test recipe products
        testRecipeProduct1 = new RecipeProduct();
        testRecipeProduct1.setId(1L);
        testRecipeProduct1.setProduct(testProduct1);
        testRecipeProduct1.setQuantity(2);

        testRecipeProduct2 = new RecipeProduct();
        testRecipeProduct2.setId(2L);
        testRecipeProduct2.setProduct(testProduct2);
        testRecipeProduct2.setQuantity(1);

        // Create test recipe
        testRecipe = new Recipe();
        testRecipe.setId(1L);
        testRecipe.setName("Test Recipe");
        testRecipe.setRecipeProducts(Arrays.asList(testRecipeProduct1, testRecipeProduct2));
    }

    @Test
    void getAllCarts_ShouldReturnAllCarts() {
        // Given
        List<Cart> expectedCarts = Arrays.asList(testCart);
        when(cartRepository.findAllCartsBasicData()).thenReturn(expectedCarts);

        // When
        List<Cart> result = cartService.getAllCarts();

        // Then
        assertEquals(expectedCarts, result);
        verify(cartRepository).findAllCartsBasicData();
    }

    @Test
    void getCartById_WhenCartExists_ShouldReturnCartResponse() {
        // Given
        List<CartItem> cartItems = Arrays.asList(testCartItem1, testCartItem2);
        testCart.setCartItems(cartItems); // Set the cart items directly on the cart
        when(cartRepository.findByIdWithItemsAndProducts(1L)).thenReturn(Optional.of(testCart));

        // When
        Optional<CartResponse> result = cartService.getCartById(1L);

        // Then
        assertTrue(result.isPresent());
        CartResponse cartResponse = result.get();
        assertEquals(1L, cartResponse.getId());
        assertEquals(1000, cartResponse.getTotalAmount());
        assertEquals(2, cartResponse.getCartItems().size());
        
        CartItemResponse item1 = cartResponse.getCartItems().get(0);
        assertEquals(1L, item1.getId());
        assertEquals(1L, item1.getProductId());
        assertEquals("Tomatoes", item1.getProductName());
        assertEquals(399, item1.getPriceInCents());
        assertEquals(2, item1.getQuantity());

        verify(cartRepository).findByIdWithItemsAndProducts(1L);
    }

    @Test
    void getCartById_WhenCartNotExists_ShouldReturnEmpty() {
        // Given
        when(cartRepository.findByIdWithItemsAndProducts(1L)).thenReturn(Optional.empty());

        // When
        Optional<CartResponse> result = cartService.getCartById(1L);

        // Then
        assertFalse(result.isPresent());
        verify(cartRepository).findByIdWithItemsAndProducts(1L);
    }

    @Test
    void addRecipeToCart_WhenCartAndRecipeExist_ShouldAddRecipeSuccessfully() {
        // Given
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(recipeRepository.findByIdWithProductsAndDetails(1L)).thenReturn(Optional.of(testRecipe));
        when(cartItemRepository.findByCartId(1L)).thenReturn(new java.util.ArrayList<>());

        // When
        String result = cartService.addRecipeToCart(1L, 1L);

        // Then
        assertEquals("Recipe added to cart successfully", result);
        verify(cartRepository).findById(1L);
        verify(recipeRepository).findByIdWithProductsAndDetails(1L);
        verify(cartItemRepository).findByCartId(1L);
        verify(cartItemRepository, times(2)).save(any(CartItem.class));
        verify(cartRepository).save(testCart);
        
        // Verify total amount was updated (399*2 + 250*1 = 1048)
        assertEquals(2048, testCart.getTotalAmount()); // 1000 + 1048
    }

    @Test
    void addRecipeToCart_WhenCartNotFound_ShouldReturnNull() {
        // Given
        when(cartRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        String result = cartService.addRecipeToCart(1L, 1L);

        // Then
        assertNull(result);
        verify(cartRepository).findById(1L);
        verify(recipeRepository).findByIdWithProductsAndDetails(1L);
        verify(cartItemRepository, never()).findByCartId(anyLong());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addRecipeToCart_WhenRecipeNotFound_ShouldReturnErrorMessage() {
        // Given
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(recipeRepository.findByIdWithProductsAndDetails(1L)).thenReturn(Optional.empty());

        // When
        String result = cartService.addRecipeToCart(1L, 1L);

        // Then
        assertEquals("Recipe not found", result);
        verify(cartRepository).findById(1L);
        verify(recipeRepository).findByIdWithProductsAndDetails(1L);
        verify(cartItemRepository, never()).findByCartId(anyLong());
        verify(cartItemRepository, never()).save(any(CartItem.class));
    }

    @Test
    void addRecipeToCart_WhenProductAlreadyInCart_ShouldUpdateQuantity() {
        // Given
        List<CartItem> existingItems = new java.util.ArrayList<>(Arrays.asList(testCartItem1));
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(recipeRepository.findByIdWithProductsAndDetails(1L)).thenReturn(Optional.of(testRecipe));
        when(cartItemRepository.findByCartId(1L)).thenReturn(existingItems);

        // When
        String result = cartService.addRecipeToCart(1L, 1L);

        // Then
        assertEquals("Recipe added to cart successfully", result);
        assertEquals(4, testCartItem1.getQuantity()); // 2 + 2 = 4
        verify(cartItemRepository).save(testCartItem1);
        verify(cartItemRepository, times(2)).save(any(CartItem.class)); // Save updated tomato + new onions item
    }

    @Test
    void removeRecipeFromCart_WhenCartAndRecipeExist_ShouldRemoveRecipeSuccessfully() {
        // Given
        List<CartItem> cartItems = new java.util.ArrayList<>(Arrays.asList(testCartItem1, testCartItem2));
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(recipeRepository.findByIdWithProductsAndDetails(1L)).thenReturn(Optional.of(testRecipe));
        when(cartItemRepository.findByCartId(1L)).thenReturn(cartItems);

        // When
        String result = cartService.removeRecipeFromCart(1L, 1L);

        // Then
        assertEquals("Recipe removed from cart successfully", result);
        verify(cartRepository).findById(1L);
        verify(recipeRepository).findByIdWithProductsAndDetails(1L);
        verify(cartItemRepository).findByCartId(1L);
        verify(cartRepository).save(testCart);
        
        // Should reduce total by recipe amount (399*2 + 250*1 = 1048)
        assertEquals(0, testCart.getTotalAmount()); // max(0, 1000 - 1048) = 0
    }

    @Test
    void removeRecipeFromCart_WhenCartNotFound_ShouldReturnNull() {
        // Given
        when(cartRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        String result = cartService.removeRecipeFromCart(1L, 1L);

        // Then
        assertNull(result);
        verify(cartRepository).findById(1L);
        verify(recipeRepository).findByIdWithProductsAndDetails(1L);
    }

    @Test
    void removeRecipeFromCart_WhenRecipeNotFound_ShouldReturnErrorMessage() {
        // Given
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(recipeRepository.findByIdWithProductsAndDetails(1L)).thenReturn(Optional.empty());

        // When
        String result = cartService.removeRecipeFromCart(1L, 1L);

        // Then
        assertEquals("Recipe not found", result);
        verify(cartRepository).findById(1L);
        verify(recipeRepository).findByIdWithProductsAndDetails(1L);
        verify(cartItemRepository, never()).findByCartId(anyLong());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void removeRecipeFromCart_WhenQuantityExceedsCartItem_ShouldDeleteCartItem() {
        // Given
        testCartItem1.setQuantity(1); // Less than recipe requirement (2)
        List<CartItem> cartItems = new java.util.ArrayList<>(Arrays.asList(testCartItem1));
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(recipeRepository.findByIdWithProductsAndDetails(1L)).thenReturn(Optional.of(testRecipe));
        when(cartItemRepository.findByCartId(1L)).thenReturn(cartItems);

        // When
        String result = cartService.removeRecipeFromCart(1L, 1L);

        // Then
        assertEquals("Recipe removed from cart successfully", result);
        verify(cartItemRepository).delete(testCartItem1);
        verify(cartItemRepository, never()).save(testCartItem1);
    }

    @Test
    void removeRecipeFromCart_WhenQuantityLessThanCartItem_ShouldReduceQuantity() {
        // Given
        testCartItem1.setQuantity(5); // More than recipe requirement (2)
        List<CartItem> cartItems = new java.util.ArrayList<>(Arrays.asList(testCartItem1));
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(recipeRepository.findByIdWithProductsAndDetails(1L)).thenReturn(Optional.of(testRecipe));
        when(cartItemRepository.findByCartId(1L)).thenReturn(cartItems);

        // When
        String result = cartService.removeRecipeFromCart(1L, 1L);

        // Then
        assertEquals("Recipe removed from cart successfully", result);
        assertEquals(3, testCartItem1.getQuantity()); // 5 - 2 = 3
        verify(cartItemRepository).save(testCartItem1);
        verify(cartItemRepository, never()).delete(any(CartItem.class));
    }
}