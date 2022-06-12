package se.magnus.microservices.core.recommendation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.test.StepVerifier;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.microservices.core.recommendation.persistence.RecommendationRepository;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
class RecommendationServiceApplicationTests {
	@Autowired
	private WebTestClient client;

  @Autowired
  private RecommendationRepository repository;

  @BeforeEach
  public void setupDb() {
    repository.deleteAll();
  }

	@Test
	public void getRecommendationsByProductId() {
		int productId = 1;

		postAndVerifyRecommendation(productId, 1, OK);
		postAndVerifyRecommendation(productId, 2, OK);
		postAndVerifyRecommendation(productId, 3, OK);

    StepVerifier.create(repository.findByProductId(productId))
      .expectNextCount(3)
      .verifyComplete();

		getAndVerifyRecommendationsByProductId(productId, OK)
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[1].productId").isEqualTo(productId)
			.jsonPath("$[1].recommendationId").isEqualTo(2);
	}

  @Test
	public void deleteRecommendations() {

		int productId = 1;
		int recommendationId = 1;

		postAndVerifyRecommendation(productId, recommendationId, OK);
		StepVerifier.create(repository.findByProductId(productId))
      .expectNextCount(1)
      .verifyComplete();

		deleteAndVerifyRecommendationsByProductId(productId, OK);
		getAndVerifyRecommendationsByProductId(productId, OK)
    .jsonPath("$.length()").isEqualTo(0);
	}

  @Test
	public void getRecommendationsMissingParameter() {

		getAndVerifyRecommendationsByProductId("/", NOT_FOUND)
			.jsonPath("$.path").isEqualTo("/recommendation/");
	}

	@Test
	public void getRecommendationsInvalidParameter() {

		getAndVerifyRecommendationsByProductId("/no-integer", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/recommendation/no-integer")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getRecommendationsNotFound() {

		getAndVerifyRecommendationsByProductId(113, OK)
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getRecommendationsInvalidParameterNegativeValue() {

		int productIdInvalid = -1;

		getAndVerifyRecommendationsByProductId(productIdInvalid, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/recommendation/-1")
			.jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus) {
		return getAndVerifyRecommendationsByProductId("/" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(String productIdQuery, HttpStatus expectedStatus) {
		return client.get()
			.uri("/recommendation" + productIdQuery)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyRecommendation(int productId, int recommendationId, HttpStatus expectedStatus) {
		Recommendation recommendation = new Recommendation(productId, recommendationId, "Author " + recommendationId, recommendationId, "Content " + recommendationId, "SA");
		return client.post()
			.uri("/recommendation")
			.body(just(recommendation), Recommendation.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyRecommendationsByProductId(int productId, HttpStatus expectedStatus) {
		return client.delete()
			.uri("/recommendation/" + productId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectBody();
	}
}
