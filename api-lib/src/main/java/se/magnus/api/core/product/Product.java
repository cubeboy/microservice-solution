package se.magnus.api.core.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Product {

    private int productId;
    private String name;
    private int weight;
    private String serviceAddress;
}
