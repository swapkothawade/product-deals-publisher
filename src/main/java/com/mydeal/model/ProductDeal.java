package com.mydeal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class ProductDeal {
    private String store;
    private String category;
    private String item;
    private String salePrice;
    private String originalPrice;
    private String productUrl;

}
