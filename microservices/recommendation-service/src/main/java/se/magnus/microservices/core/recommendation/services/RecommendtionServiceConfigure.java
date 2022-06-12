package se.magnus.microservices.core.recommendation.services;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import se.magnus.api.core.product.ProductServiceUri;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.http.MediaType.*;

@EnableWebFlux
@Configuration
public class RecommendtionServiceConfigure {
  @Bean
  public RouterFunction<ServerResponse> route(RecommendationHandler handler) {
    RouterFunction<ServerResponse> route = RouterFunctions.route()
      .GET(ProductServiceUri.RECOMMENDATION + "/{productId}", accept(APPLICATION_JSON), handler::getRecommendations)
      .POST(ProductServiceUri.RECOMMENDATION, accept(APPLICATION_JSON), handler::createRecommendation)
      .DELETE(ProductServiceUri.RECOMMENDATION + "/{productId}", accept(APPLICATION_JSON), handler::deleteRecommendation)
      .build();

    return route;
  }
}
