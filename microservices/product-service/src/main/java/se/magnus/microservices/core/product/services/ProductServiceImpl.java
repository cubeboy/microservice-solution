package se.magnus.microservices.core.product.services;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ProductServiceImpl implements ProductService {
  private final ServiceUtil serviceUtil;

  @Override
  public Product getProduct(int productId) {
    log.debug("/product return the found product for productId={}", productId);
    if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
    if (productId == 13) throw new NotFoundException("No product found for productId: " + productId);

    return new Product(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());
  }
  
}