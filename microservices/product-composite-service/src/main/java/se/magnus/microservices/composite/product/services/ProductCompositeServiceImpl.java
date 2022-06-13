package se.magnus.microservices.composite.product.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.composite.product.PorductCompositeService;
import se.magnus.api.composite.product.ProductAggregate;
import se.magnus.api.composite.product.RecommendationSummary;
import se.magnus.api.composite.product.ReviewSummary;
import se.magnus.api.composite.product.ServiceAddresses;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.util.http.ServiceUtil;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ProductCompositeServiceImpl implements PorductCompositeService {
  private final ServiceUtil serviceUtil;
  private final ProductCompositeIntegration integration;

  @Override
  public void createCompositeProduct(ProductAggregate body) {
    log.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.getProductId());
    Product product = Product.builder()
      .productId(body.getProductId())
      .name(body.getName())
      .weight(body.getWeight()).build();
    integration.createProduct(product);


    if (body.getRecommendations() != null) {
      body.getRecommendations().forEach(r -> {
        integration.createRecommendation(Recommendation.builder()
          .productId(body.getProductId())
          .recommendationId(r.getRecommendationId())
          .author(r.getAuthor())
          .rate(r.getRate())
          .content(r.getContent()).build());
      });
    }

    if (body.getReviews() != null) {
      body.getReviews().forEach(r -> {
        integration.createReview(Review.builder()
          .productId(body.getProductId())
          .reviewId(r.getReviewId())
          .author(r.getAuthor())
          .subject(r.getSubject())
          .content(r.getContent()).build());
      });
    }

    log.debug("createCompositeProduct: composite entites created for productId: {}", body.getProductId());
  }

  @Override
  public ProductAggregate getCompositeProduct(int productId) {
    Mono<Product> product = integration.getProduct(productId);
    Flux<Recommendation> recommendations = integration.getRecommendations(productId);
    Flux<Review> reviews = integration.getReviews(productId);

    ProductAggregate aggregate = new ProductAggregate();
    ServiceAddresses addresses = new ServiceAddresses();
    addresses.setCompositeProduct(serviceUtil.getServiceAddress());

    product.doOnNext(p -> {
      aggregate.setProductId(p.getProductId());
      aggregate.setName(p.getName());
      aggregate.setWeight(p.getWeight());
      addresses.setProduct(p.getServiceAddress());
    }).subscribe();

    recommendations.doOnNext(r -> {
      RecommendationSummary summary = RecommendationSummary.builder()
        .recommendationId(r.getRecommendationId())
        .author(r.getAuthor())
        .rate(r.getRate())
        .content(r.getContent()).build();
      aggregate.getRecommendations().add(summary);
      addresses.setRecommendation(r.getServiceAddress());
    }).subscribe();

    reviews.doOnNext(rev -> {
      ReviewSummary summary = ReviewSummary.builder()
        .reviewId(rev.getReviewId())
        .author(rev.getAuthor())
        .subject(rev.getSubject())
        .content(rev.getContent()).build();
      aggregate.getReviews().add(summary);
      addresses.setReview(rev.getServiceAddress());
    }).subscribe();

    aggregate.setServiceAddresses(addresses);
    return aggregate;
  }

  @Override
  public void deleteCompositeProduct(int productId) {
    log.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

    integration.deleteProduct(productId);

    integration.deleteRecommendations(productId);

    integration.deleteReviews(productId);

    log.debug("getCompositeProduct: aggregate entities deleted for productId: {}", productId);
  }
}
