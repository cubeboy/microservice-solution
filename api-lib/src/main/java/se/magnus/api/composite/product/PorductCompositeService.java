package se.magnus.api.composite.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface PorductCompositeService {
  @GetMapping(
    value="product-composite/{productId}",
    produces="application/json"
  )
  ProductAggregate getProduct(@PathVariable int productId);
}
