package se.magnus.api.core.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    private int productId;
    private String name;
    private int weight;
    private String serviceAddress;
}
