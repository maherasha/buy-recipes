package com.buyrecipe.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeProductResponse {
    private Long productId;
    private String productName;
    private Integer priceInCents;
    private Integer quantity;
    
}