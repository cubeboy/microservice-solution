package se.magnus.microservices.core.recommendation.services;

//import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.microservices.core.recommendation.persistence.RecommendationEntity;
import se.magnus.microservices.core.recommendation.persistence.RecommendationRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import static reactor.core.publisher.Mono.error;

@Slf4j
@RequiredArgsConstructor
//@Component
public class RecommendationHandler {
  private final RecommendationRepository repository;
  private final ServiceUtil serviceUtil;

  public Mono<ServerResponse> getRecommendations(ServerRequest request) {
    String paramProductId = request.pathVariable("productId");
    int productId = Integer.parseInt(paramProductId);
    log.info("getRecommendations :: productId = " + productId);
    if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

    Flux<Recommendation> resRecommendation = repository.findByProductId(productId)
      .flatMap(entity -> Mono.just(entity.toRecommendation(serviceUtil.getServiceAddress())));

    return ServerResponse.ok().body(resRecommendation, Recommendation.class);
  }

  public Mono<ServerResponse> createRecommendation(ServerRequest request) {
    return request.bodyToMono(Recommendation.class)
      .flatMap(body -> {
        if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());
        RecommendationEntity entity = new RecommendationEntity(body);

        repository.save(entity).subscribe();
        body = entity.toRecommendation(serviceUtil.getServiceAddress());

        return ServerResponse.ok().body(Mono.just(body), Recommendation.class);
      });
  }

  public Mono<ServerResponse> deleteRecommendation(ServerRequest request) {
    String paramProductId = request.pathVariable("productId");
    int productId = Integer.parseInt(paramProductId);
    log.info("deleteRecommendation :: productId = " + productId);
    if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

    repository.findByProductId(productId)
      .switchIfEmpty(error(new NotFoundException("No recommendation found for productId: " + productId)))
      .doOnNext(entity -> repository.delete(entity).subscribe())
      .subscribe();

    return ServerResponse.ok().build();
  }
}
