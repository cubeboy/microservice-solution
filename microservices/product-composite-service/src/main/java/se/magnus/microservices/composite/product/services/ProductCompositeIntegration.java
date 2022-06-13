package se.magnus.microservices.composite.product.services;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;

@Slf4j
@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {
  private final ObjectMapper mapper;

  private final String productServiceUrl;
  private final String recommendationServiceUrl;
  private final String reviewServiceUrl;

  @Autowired
  public ProductCompositeIntegration(
    RestTemplate restTemplate,
    ObjectMapper mapper,

    @Value("${app.product-service.host}") String productServiceHost,
    @Value("${app.product-service.port}") int    productServicePort,

    @Value("${app.recommendation-service.host}") String recommendationServiceHost,
    @Value("${app.recommendation-service.port}") int    recommendationServicePort,

    @Value("${app.review-service.host}") String reviewServiceHost,
    @Value("${app.review-service.port}") int    reviewServicePort) {

    this.mapper = mapper;

    productServiceUrl        = "http://" + productServiceHost + ":" + productServicePort + "/product/";
    recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendation?productId=";
    reviewServiceUrl         = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review?productId=";
  }


  @Override
  public Flux<Review> getReviews(int productId) {  
    String url = reviewServiceUrl + productId;

    log.debug("Will call getReviews API on URL: {}", url);      
    WebClient client = WebClient.builder()
      .baseUrl(url)
      .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .build();
    return client.get()
      .retrieve()
      .bodyToFlux(Review.class)
      .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
  }

  @Override
  public Flux<Recommendation> getRecommendations(int productId) {
    String url = recommendationServiceUrl + productId;

    log.debug("Will call getRecommendations API on URL: {}", url);
    WebClient client = WebClient.builder()
      .baseUrl(url)
      .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .build();
    return client.get()
      .retrieve()
      .bodyToFlux(Recommendation.class)
      .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
  }

  @Override
  public Mono<Product> getProduct(int productId) {
    String url = productServiceUrl + productId;
    log.debug("Will call getProduct API on URL: {}", url);

    WebClient client = WebClient.builder()
      .baseUrl(url)
      .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .build();
    return client.get()
      .retrieve()
      .bodyToMono(Product.class)
      .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
  }

  @Override
  public Mono<Product> createProduct(Product body) {
    String url = productServiceUrl;
    log.debug("Will post a new product to URL: {}", url);

    WebClient client = WebClient.builder()
      .baseUrl(url)
      .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .build();
    return client.post()
      .body(body, Product.class)
      .retrieve()
      .bodyToMono(Product.class)
      .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
  }

  @Override
  public void deleteProduct(int productId) {
    String url = productServiceUrl + "/" + productId;
    log.debug("Will call the deleteProduct API on URL: {}", url);
    WebClient client = WebClient.builder()
      .baseUrl(url)
      .build();
    client.delete();
  }

  @Override
  public Mono<Recommendation> createRecommendation(Recommendation body) {
    String url = recommendationServiceUrl;
    log.debug("Will post a new recommendation to URL: {}", url);
    WebClient client = WebClient.builder()
      .baseUrl(url)
      .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .build();
    return client.post()
      .body(body, Recommendation.class)
      .retrieve()
      .bodyToMono(Recommendation.class)
      .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
  }

  @Override
  public void deleteRecommendations(int productId) {
    String url = recommendationServiceUrl + "?productId=" + productId;
    log.debug("Will call the deleteRecommendations API on URL: {}", url);
    WebClient client = WebClient.builder()
      .baseUrl(url)        
      .build();
    client.delete();
  }

  @Override
  public Mono<Review> createReview(Review body) {
      String url = reviewServiceUrl;
      log.debug("Will post a new review to URL: {}", url);
      WebClient client = WebClient.builder()
      .baseUrl(url)
      .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .build();
    return client.post()
      .body(body, Review.class)
      .retrieve()
      .bodyToMono(Review.class)
      .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
  }

  @Override
  public void deleteReviews(int productId) {
    String url = reviewServiceUrl + "?productId=" + productId;
    log.debug("Will call the deleteReviews API on URL: {}", url);
    WebClient client = WebClient.builder()
      .baseUrl(url)
      .build();
    client.delete();
  }

  private Throwable handleException(Throwable ex) {

    if (!(ex instanceof WebClientResponseException)) {
        log.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
        return ex;
    }

    WebClientResponseException wcre = (WebClientResponseException)ex;

    switch (wcre.getStatusCode()) {

    case NOT_FOUND:
        return new NotFoundException(getErrorMessage(wcre));

    case UNPROCESSABLE_ENTITY :
        return new InvalidInputException(getErrorMessage(wcre));

    default:
        log.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
        log.warn("Error body: {}", wcre.getResponseBodyAsString());
        return ex;
    }
}

  private String getErrorMessage(WebClientResponseException ex) {
    try {
      return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
    } catch (IOException ioex) {
      return ex.getMessage();
    }
  }
}
