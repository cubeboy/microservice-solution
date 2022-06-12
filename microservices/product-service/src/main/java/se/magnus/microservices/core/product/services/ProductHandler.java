package se.magnus.microservices.core.product.services;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import se.magnus.api.core.product.Product;
import se.magnus.microservices.core.product.persistence.ProductEntity;
import se.magnus.microservices.core.product.persistence.ProductRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import static reactor.core.publisher.Mono.error;

@Slf4j
@RequiredArgsConstructor
// @Component
public class ProductHandler {
  private final ProductRepository repository;
  private final ServiceUtil serviceUtil;

  public Mono<ServerResponse> getProduct(ServerRequest request){
    String paramProductId = request.pathVariable("productId");
    int productId = Integer.parseInt(paramProductId);
    log.info("getProduct :: productId = " + productId);
    if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

    return repository.findByProductId(productId)
      .switchIfEmpty(error(new NotFoundException("No product found for productId: " + productId)))
      .flatMap(o -> {
        Product resBody = o.toProduct(serviceUtil.getServiceAddress());
        return ServerResponse.ok().body(Mono.just(resBody), Product.class);
      });
  }

  public Mono<ServerResponse> createProduct(ServerRequest request) {
    return request.bodyToMono(Product.class)
      .flatMap(body -> {
        if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());
        ProductEntity entity = new ProductEntity(body);

        repository.save(entity).subscribe();
        body = entity.toProduct(serviceUtil.getServiceAddress());
        return ServerResponse.ok().body(Mono.just(body), Product.class);
      });
  }

  public Mono<ServerResponse> deleteProduct(ServerRequest request) {
    String paramProductId = request.pathVariable("productId");
    int productId = Integer.parseInt(paramProductId);
    log.info("getProduct :: productId = " + productId);
    if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

    repository.findByProductId(productId)
      .switchIfEmpty(error(new NotFoundException("No product found for productId: " + productId)))
      .doOnNext(entity -> repository.delete(entity).subscribe())
      .subscribe();
    return ServerResponse.ok().build();
  }
}
