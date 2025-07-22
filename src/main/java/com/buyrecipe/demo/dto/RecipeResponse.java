package com.buyrecipe.demo.dto;

import java.util.List;

public class RecipeResponse {
    private Long id;
    private String name;
    private List<RecipeProductResponse> products;
    
    public RecipeResponse() {}
    
    public RecipeResponse(Long id, String name, List<RecipeProductResponse> products) {
        this.id = id;
        this.name = name;
        this.products = products;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<RecipeProductResponse> getProducts() {
        return products;
    }
    
    public void setProducts(List<RecipeProductResponse> products) {
        this.products = products;
    }
}