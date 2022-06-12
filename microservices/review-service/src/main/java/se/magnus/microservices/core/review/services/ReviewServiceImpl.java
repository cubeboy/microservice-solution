package se.magnus.microservices.core.review.services;

import java.util.List;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.EnableWebFlux;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.microservices.core.review.persistence.ReviewEntity;
import se.magnus.microservices.core.review.persistence.ReviewRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@Slf4j
@RequiredArgsConstructor
@EnableWebFlux
@RestController
public class ReviewServiceImpl implements ReviewService {

  private final ServiceUtil serviceUtil;

  private final ReviewRepository repository;

  @Override
    public Mono<Review> createReview(Review body) {
      if (body.getProductId() < 1) throw new InvalidInputException("Invalid productId: " + body.getProductId());

      return Mono.just(body)
        .map(p -> {
          ReviewEntity entity = new ReviewEntity(p);
          entity = repository.save(entity);
          return entity.toReview(serviceUtil.getServiceAddress());
        });
    }

    @Override
    public Flux<Review> getReviews(int productId) {

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        return Flux.just(productId)
          .flatMap(pId -> {
            List<ReviewEntity> reviews = repository.findByProductId(pId);
            Flux<ReviewEntity> fReviews = Flux.fromIterable(reviews);
            return fReviews.flatMap(entity -> Mono.just(entity.toReview(serviceUtil.getServiceAddress())));
          });
    }

    @Override
    public void deleteReviews(int productId) {
        log.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
        Mono.just(productId).doOnNext(pId -> {
          List<ReviewEntity> reviews = repository.findByProductId(pId);
          if(reviews.size() == 0)
            throw new NotFoundException("No product found for productId: " + productId);

          repository.deleteAll(reviews);
        })
        .onErrorResume(RuntimeException.class, e -> Mono.error(e))
        .subscribe();
    }
}
