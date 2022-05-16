package se.magnus.microservices.composite.product.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.magnus.api.composite.product.PorductCompositeService;
import se.magnus.api.composite.product.ProductAggregate;
import se.magnus.api.composite.product.RecommendationSummary;
import se.magnus.api.composite.product.ReviewSummary;
import se.magnus.api.composite.product.ServiceAddresses;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.util.exceptions.NotFoundException;
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
  public ProductAggregate getProduct(int productId) {
    log.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productId);

    Product product = integration.getProduct(productId);
    if (product == null) throw new NotFoundException("No product found for productId: " + productId);

    List<Recommendation> recommendations = integration.getRecommendations(productId);

    List<Review> reviews = integration.getReviews(productId);

    log.debug("getCompositeProduct: aggregate entity found for productId: {}", productId);

    return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
  }

  @Override
  public void deleteCompositeProduct(int productId) {
    log.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

    integration.deleteProduct(productId);

    integration.deleteRecommendations(productId);

    integration.deleteReviews(productId);

    log.debug("getCompositeProduct: aggregate entities deleted for productId: {}", productId);
  }


  private ProductAggregate createProductAggregate(
      Product product
    , List<Recommendation> recommendations
    , List<Review> reviews
    , String serviceAddress) {
    
    int productId = product.getProductId();
    String name = product.getName();
    int weight = product.getWeight();

    List<RecommendationSummary> recommendationSummaries = (recommendations == null) ? null :
      recommendations.stream()
        .map(r -> RecommendationSummary.builder()
          .recommendationId(r.getRecommendationId())
          .author(r.getAuthor())
          .rate(r.getRate())
          .content(r.getContent()).build())
        .collect(Collectors.toList());
    List<ReviewSummary> reviewSummaries = (reviews == null) ? null :
      reviews.stream()
        .map(r -> ReviewSummary.builder()
          .reviewId(r.getReviewId())
          .author(r.getAuthor())
          .subject(r.getSubject())
          .content(r.getContent()).build())
        .collect(Collectors.toList());

    String productAddress = product.getServiceAddress();
    String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
    String recommendationAddress = (recommendations != null & recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
    ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

    return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
  }


}
