package se.magnus.microservices.core.product.services;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.EnableWebFlux;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.microservices.core.product.persistence.ProductEntity;
import se.magnus.microservices.core.product.persistence.ProductRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@EnableWebFlux
@RequiredArgsConstructor
@RestController
public class ProductServiceImpl implements ProductService {
  private final ProductRepository repository;
  private final ServiceUtil serviceUtil;
  @Override
  public Mono<Product> createProduct(Product body) {
    if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());

    return Mono.just(body)
      .map(p -> {
        ProductEntity entity = new ProductEntity(p);
        repository.save(entity).subscribe();
        return entity.toProduct(serviceUtil.getServiceAddress());
      });
  }

  @Override
  public Mono<Product> getProduct(int productId) {
    if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

    return repository.findByProductId(productId)
      .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
      .flatMap(entity -> Mono.just(entity.toProduct(serviceUtil.getServiceAddress())));
  }

  @Override
  public void deleteProduct(int productId) {
    if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
    repository.findByProductId(productId)
      .switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
      .doOnNext(entity -> repository.delete(entity).subscribe())
      .subscribe();
  }

}
