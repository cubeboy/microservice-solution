package se.magnus.microservices.core.product.productservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.test.StepVerifier;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductServiceUri;
import se.magnus.microservices.core.product.persistence.ProductRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties={"spring.data.mongodb.port: 0"})
class ProductServiceApplicationTests {
	@Autowired
	private WebTestClient client;

  @Autowired
  private ProductRepository repository;

  @BeforeEach
  public void setupDb() {
    repository.deleteAll();
  }

	@Test
	public void getProductById() {
		int productId = 1;

    assertNull(repository.findByProductId(productId).block());
		assertEquals(0, (long)repository.count().block());

		postAndVerifyProduct(productId, OK).jsonPath("$.productId").isEqualTo(productId);

		assertNotNull(repository.findByProductId(productId).block());
		assertEquals(1, (long)repository.count().block());

		getAndVerifyProduct(productId, OK)
      .jsonPath("$.productId").isEqualTo(productId);
	}

  @Test
	public void deleteProduct() {

		int productId = 5;

		postAndVerifyProduct(productId, OK).jsonPath("$.productId").isEqualTo(productId);
		StepVerifier.create(repository.findByProductId(productId))
      .expectNextMatches(entity -> entity.getProductId() == productId);

		deleteAndVerifyProduct(productId, OK);
    StepVerifier.create(repository.findByProductId(productId))
      .expectNextMatches(entity -> entity == null);
	}

	@Test
	public void getProductInvalidParameterString() {
      client.get()
        .uri(ProductServiceUri.PRODUCT + "/" + "no-integer")
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isEqualTo(BAD_REQUEST)
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.path").isEqualTo("/product/no-integer")
        .jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getProductNotFound() {

		int productIdNotFound = 13;
		getAndVerifyProduct(productIdNotFound, NOT_FOUND)
      .jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
      .jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
	}


	@Test
	public void getProductInvalidParameterNegativeValue() {

    int productIdInvalid = -1;
		getAndVerifyProduct(productIdInvalid, UNPROCESSABLE_ENTITY)
            .jsonPath("$.path").isEqualTo("/product/" + productIdInvalid)
            .jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return client.get()
			.uri(ProductServiceUri.PRODUCT + "/" + productId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		Product product = new Product(productId, "Name " + productId, productId, "SA");
		return client.post()
			.uri(ProductServiceUri.PRODUCT)
			.body(just(product), Product.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return client.delete()
      .uri(ProductServiceUri.PRODUCT + "/" + productId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectBody();
	}
}
