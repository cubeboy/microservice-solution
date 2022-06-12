package se.magnus.microservices.core.recommendation.services;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.EnableWebFlux;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.microservices.core.recommendation.persistence.RecommendationEntity;
import se.magnus.microservices.core.recommendation.persistence.RecommendationRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@EnableWebFlux
@RequiredArgsConstructor
@RestController
public class RecommendationServiceImpl implements RecommendationService {
  private final RecommendationRepository repository;
  private final ServiceUtil serviceUtil;

  @Override
  public Mono<Recommendation> createRecommendation(Recommendation body) {
    if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());

    return Mono.just(body)
      .map(p -> {
        RecommendationEntity entity = new RecommendationEntity(p);
        repository.save(entity).subscribe();
        return entity.toRecommendation(serviceUtil.getServiceAddress());
      });
  }

  @Override
  public Flux<Recommendation> getRecommendations(int productId) {
    if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

    return repository.findByProductId(productId)
      .flatMap(entity -> Mono.just(entity.toRecommendation(serviceUtil.getServiceAddress())));
  }

  @Override
  public void deleteRecommendations(int productId) {
    if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
    repository.findByProductId(productId)
      .switchIfEmpty(Mono.error(new NotFoundException("No recommendation found for productId: " + productId)))
      .doOnNext(entity -> repository.delete(entity).subscribe())
      .subscribe();
  }
}
