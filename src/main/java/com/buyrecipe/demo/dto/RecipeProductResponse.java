package com.buyrecipe.demo.dto;

public class RecipeProductResponse {
    private Long productId;
    private String productName;
    private Integer priceInCents;
    private Integer quantity;
    
    public RecipeProductResponse() {}
    
    public RecipeProductResponse(Long productId, String productName, Integer priceInCents, Integer quantity) {
        this.productId = productId;
        this.productName = productName;
        this.priceInCents = priceInCents;
        this.quantity = quantity;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public Integer getPriceInCents() {
        return priceInCents;
    }
    
    public void setPriceInCents(Integer priceInCents) {
        this.priceInCents = priceInCents;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}