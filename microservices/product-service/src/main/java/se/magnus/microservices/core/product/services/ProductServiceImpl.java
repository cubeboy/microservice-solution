package se.magnus.microservices.core.product.services;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.microservices.core.product.persistence.ProductEntity;
import se.magnus.microservices.core.product.persistence.ProductRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import static reactor.core.publisher.Mono.error;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ProductServiceImpl implements ProductService {
private final ServiceUtil serviceUtil;
  private final ProductRepository repository;

  @Override
  public Product createProduct(Product body) {
    if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());

    ProductEntity entity = new ProductEntity(body);
    Mono<Product> newEntity = repository.save(entity)
      .log()
      .onErrorMap(
        DuplicateKeyException.class
        , ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId()))
      .map(e -> e.toProduct(serviceUtil.getServiceAddress()));

    Product ret = newEntity.block();
    return ret;
  }

  @Override
  public Mono<Product> getProduct(int productId) {
    log.debug("/product return the found product for productId={}", productId);
    if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

    return repository.findByProductId(productId)
      .switchIfEmpty(error(new NotFoundException("No product found for productId: " + productId)))
      .log()
      .map(e -> e.toProduct(serviceUtil.getServiceAddress()));
  }

  @Override
  public void deleteProduct(int productId) {
    log.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
    //repository.findByProductId(productId).map(e -> if e != null ) .ifPresent(e -> repository.delete(e));
  }
}
