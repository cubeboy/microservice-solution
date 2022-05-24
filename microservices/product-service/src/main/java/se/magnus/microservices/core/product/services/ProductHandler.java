package se.magnus.microservices.core.product.services;

import org.springframework.http.HttpStatus;
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
import se.magnus.util.http.HttpErrorInfo;
import se.magnus.util.http.ServiceUtil;

import static reactor.core.publisher.Mono.error;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductHandler {
  private final ProductRepository repository;
  private final ServiceUtil serviceUtil;

  public Mono<ServerResponse> getProduct(ServerRequest request) throws RuntimeException {
    return Mono.just(Product.builder().build())
      .flatMap( p -> {
        String paramProductId = request.pathVariable("productId");
        int productId = Integer.parseInt(paramProductId);
        log.info("getProduct :: productId = " + productId);
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        return repository.findByProductId(productId)
          .switchIfEmpty(error(new NotFoundException("No product found for productId: " + productId)))
          .flatMap(o -> {
            Product resBody = o.toProduct(serviceUtil.getServiceAddress());
            return ServerResponse.ok().body(Mono.just(resBody), Product.class);
          })
          .onErrorResume(
        NotFoundException.class
            , e -> {
              HttpErrorInfo eInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, request.uri().getPath(), e.getMessage());
              return ServerResponse.status(HttpStatus.NOT_FOUND).body(Mono.just(eInfo), HttpErrorInfo.class);
          });
      })
      .onErrorResume(
        NumberFormatException.class
        , e -> {
          HttpErrorInfo eInfo = new HttpErrorInfo(HttpStatus.BAD_REQUEST, request.uri().getPath(), "Type mismatch.");
          return ServerResponse.status(HttpStatus.BAD_REQUEST).body(Mono.just(eInfo), HttpErrorInfo.class);
      })
      .onErrorResume(
        NotFoundException.class
        , e -> {
          HttpErrorInfo eInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, request.uri().getPath(), e.getMessage());
          return ServerResponse.status(HttpStatus.NOT_FOUND).body(Mono.just(eInfo), HttpErrorInfo.class);
      })
      .onErrorResume(
        InvalidInputException.class
        , e -> {
          HttpErrorInfo eInfo = new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, request.uri().getPath(), e.getMessage());
          return ServerResponse.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Mono.just(eInfo), HttpErrorInfo.class);
      });
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
    return Mono.just(Product.builder().build())
      .flatMap( p -> {
        String paramProductId = request.pathVariable("productId");
        int productId = Integer.parseInt(paramProductId);
        log.info("getProduct :: productId = " + productId);
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        return repository.findByProductId(productId)
          .switchIfEmpty(error(new NotFoundException("No product found for productId: " + productId)))
          .flatMap(e -> {
            repository.delete(e).subscribe();
            return ServerResponse.ok().build();
          })
          .onErrorResume(
        NotFoundException.class
              , e -> {
                HttpErrorInfo eInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, request.uri().getPath(), e.getMessage());
                return ServerResponse.status(HttpStatus.NOT_FOUND).body(Mono.just(eInfo), HttpErrorInfo.class);
          });
      })
      .onErrorResume(
        NumberFormatException.class
        , e -> {
          HttpErrorInfo eInfo = new HttpErrorInfo(HttpStatus.BAD_REQUEST, request.uri().getPath(), "Type mismatch.");
          return ServerResponse.status(HttpStatus.BAD_REQUEST).body(Mono.just(eInfo), HttpErrorInfo.class);
      })
      .onErrorResume(
        NotFoundException.class
        , e -> {
          HttpErrorInfo eInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, request.uri().getPath(), e.getMessage());
          return ServerResponse.status(HttpStatus.NOT_FOUND).body(Mono.just(eInfo), HttpErrorInfo.class);
      })
      .onErrorResume(
        InvalidInputException.class
        , e -> {
          HttpErrorInfo eInfo = new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, request.uri().getPath(), e.getMessage());
          return ServerResponse.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Mono.just(eInfo), HttpErrorInfo.class);
      });
  }
}
