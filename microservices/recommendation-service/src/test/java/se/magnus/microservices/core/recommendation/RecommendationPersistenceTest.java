package se.magnus.microservices.core.recommendation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.test.StepVerifier;
import se.magnus.microservices.core.recommendation.persistence.RecommendationEntity;
import se.magnus.microservices.core.recommendation.persistence.RecommendationRepository;

@ExtendWith(SpringExtension.class)
@DataMongoTest
public class RecommendationPersistenceTest {
  @Autowired
  private RecommendationRepository repository;

  private RecommendationEntity savedEntity;

  @BeforeEach
  public void setupDb(){
    repository.deleteAll();

    RecommendationEntity entity = RecommendationEntity.builder()
      .productId(1)
      .recommendationId(2)
      .author("a")
      .rating(3)
      .content("c").build();

    StepVerifier.create(repository.save(entity))
      .expectNextMatches(t -> t.equals(entity))
      .verifyComplete();
    savedEntity = entity;
  }

  @Test
  public void create() {
    RecommendationEntity newEntity = RecommendationEntity.builder()
      .productId(1)
      .recommendationId(3)
      .author("a")
      .rating(3)
      .content("c").build();
    StepVerifier.create(repository.save(newEntity))
      .expectNextMatches(t -> t.equals(newEntity))
      .verifyComplete();


    StepVerifier.create(repository.findById(newEntity.getId()))
      .expectNextMatches(t -> t.equals(newEntity))
      .verifyComplete();
  }

  @Test
   	public void update() {
      assertEquals(0, (long)savedEntity.getVersion());

      RecommendationEntity updateEntity = RecommendationEntity.builder()
        .id(savedEntity.getId())
        .version(savedEntity.getVersion())
        .productId(savedEntity.getProductId())
        .recommendationId(savedEntity.getRecommendationId())
        .author("a2")
        .rating(savedEntity.getRating())
        .content(savedEntity.getAuthor()).build();
      StepVerifier.create(repository.save(updateEntity))
        .expectNextMatches(t ->
          t.getVersion().equals(1) &&
          "a2".equals(t.getAuthor()))
        .verifyComplete();
    }

    @Test
   	public void delete() {
        repository.delete(savedEntity).subscribe();
        StepVerifier.create(repository.existsById(savedEntity.getId()))
          .expectNext(false)
          .verifyComplete();
    }

    @Test
   	public void getByProductId() {
        StepVerifier.create(repository.findByProductId(savedEntity.getProductId()))
          .thenRequest(1)
          .expectNextMatches(t -> t.equals(savedEntity))
          .verifyComplete();
    }

    @Test
   	public void duplicateError() {
      RecommendationEntity entity = RecommendationEntity.builder()
        .id(savedEntity.getId())
        .productId(1)
        .recommendationId(2)
        .author("a")
        .rating(3)
        .content("c").build();

      StepVerifier.create(repository.save(entity))
        .expectError(DuplicateKeyException.class)
        .verify();
    }

    @Test
   	public void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        RecommendationEntity entity1 = savedEntity.toBuilder().build();
        RecommendationEntity entity2 = savedEntity.toBuilder().build();

        // Update the entity using the first entity object
        entity1.setAuthor("a1");
        StepVerifier.create(repository.save(entity1))
          .expectNextMatches(e ->
            "a1".equals(e.getAuthor())
            && e.getVersion().equals(1))
          .verifyComplete();

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        entity2.setAuthor("a2");
        StepVerifier.create(repository.save(entity2))
          .expectError(OptimisticLockingFailureException.class)
          .verify();
    }
}
