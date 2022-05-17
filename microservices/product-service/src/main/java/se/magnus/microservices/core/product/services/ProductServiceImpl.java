package se.magnus.microservices.core.product.services;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.microservices.core.product.persistence.ProductEntity;
import se.magnus.microservices.core.product.persistence.ProductRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ProductServiceImpl implements ProductService {
private final ServiceUtil serviceUtil;
  private final ProductRepository repository;

  @Override
  public Product createProduct(Product body) {
    try {
      ProductEntity entity = new ProductEntity(body);
      ProductEntity newEntity = repository.save(entity);

      log.debug("createProduct: entity created for productId: {}", body.getProductId());
      return newEntity.toProduct(serviceUtil.getServiceAddress());

    } catch (DuplicateKeyException dke) {
        throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId());
    }
  }

  @Override
  public Product getProduct(int productId) {
    log.debug("/product return the found product for productId={}", productId);
    if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

    ProductEntity entity = repository.findByProductId(productId)
      .orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));
    Product response = entity.toProduct(serviceUtil.getServiceAddress());
    log.debug("getProduct: found productId: {}", response.getProductId());

    return response;
  }

  @Override
  public void deleteProduct(int productId) {
    log.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
    repository.findByProductId(productId).ifPresent(e -> repository.delete(e));
  }
}
