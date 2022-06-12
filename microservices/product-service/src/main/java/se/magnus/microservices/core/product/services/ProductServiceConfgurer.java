package se.magnus.microservices.core.product.services;

import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import se.magnus.api.core.product.ProductServiceUri;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.http.MediaType.*;

// @EnableWebFlux
// @Configuration
public class ProductServiceConfgurer {
  @Bean
  public RouterFunction<ServerResponse> route(ProductHandler handler) {
    RouterFunction<ServerResponse> route = RouterFunctions.route()
      .GET(ProductServiceUri.PRODUCT + "/{productId}", accept(APPLICATION_JSON), handler::getProduct)
      .POST(ProductServiceUri.PRODUCT, accept(APPLICATION_JSON), handler::createProduct)
      .DELETE(ProductServiceUri.PRODUCT + "/{productId}", accept(APPLICATION_JSON), handler::deleteProduct)
      .build();

    return route;
  }
}
