package se.magnus.microservices.core.product.services;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
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
@Component
public class ProductHandler {
  private final ProductRepository repository;
  private final ServiceUtil serviceUtil;

  public Mono<ServerResponse> getProduct(ServerRequest request) throws RuntimeException {
    String paramProductId = request.pathVariable("productId");
    int productId = Integer.parseInt(paramProductId);
    if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

    Mono<Product> product = repository.findByProductId(productId)
        .switchIfEmpty(error(new NotFoundException("No product found for productId: " + productId)))
        .log()
        .map(e -> e.toProduct(serviceUtil.getServiceAddress()));

    return ServerResponse.ok().body(product, Product.class);
  }

  public Mono<ServerResponse> createProduct(ServerRequest request) throws RuntimeException {
    Mono<Product> product = request.bodyToMono(Product.class);

    return product
      .doOnNext(body -> {
        if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());
        ProductEntity entity = new ProductEntity(body);

        repository.save(entity).subscribe();
        body = entity.toProduct(serviceUtil.getServiceAddress());
      })
      .flatMap(body -> {
        return ServerResponse.ok().body(Mono.just(body), Product.class);
      });
  }

  public Mono<ServerResponse> deleteProduct(ServerRequest request) {
    String paramProductId = request.pathVariable("productId");
    int productId = Integer.parseInt(paramProductId);
    if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

    log.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
    return  repository.findByProductId(productId)
      .map( e -> {
        repository.delete(e).subscribe();
        log.info("==============================================================");
        log.info(e.toString());
        return e;
      })
      .flatMap( e -> {
        return ServerResponse.ok().bodyValue("{}");
      });
  }
}
