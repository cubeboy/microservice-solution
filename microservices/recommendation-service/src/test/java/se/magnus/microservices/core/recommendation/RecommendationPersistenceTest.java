package se.magnus.microservices.core.recommendation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
    savedEntity = repository.save(entity);

    assertEquals(entity, savedEntity);
  }

  @Test
  public void create() {
    RecommendationEntity newEntity = RecommendationEntity.builder()
      .productId(1)
      .recommendationId(3)
      .author("a")
      .rating(3)
      .content("c").build();
    repository.save(newEntity);

    RecommendationEntity foundEntity = repository.findById(newEntity.getId()).get();
    assertEquals(newEntity, foundEntity);

    assertEquals(2, repository.count());
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
      savedEntity = repository.save(updateEntity);

      RecommendationEntity foundEntity = repository.findById(updateEntity.getId()).get();
      assertEquals(foundEntity, savedEntity);
      assertEquals(1, (long)foundEntity.getVersion());
      assertEquals("a2", foundEntity.getAuthor());
    }

    @Test
   	public void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
   	public void getByProductId() {
        List<RecommendationEntity> entityList = repository.findByProductId(savedEntity.getProductId());

        assertEquals(1, entityList.size());
        assertEquals(savedEntity, entityList.get(0));
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
      assertThrows(DuplicateKeyException.class, () -> {
        repository.save(entity);
      });
    }

    @Test
   	public void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        RecommendationEntity entity1 = repository.findById(savedEntity.getId()).get();
        RecommendationEntity entity2 = repository.findById(savedEntity.getId()).get();

        // Update the entity using the first entity object
        entity1.setAuthor("a1");
        repository.save(entity1);

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        assertThrows(OptimisticLockingFailureException.class, () -> {
          entity2.setAuthor("a2");
            repository.save(entity2);
        });

        // Get the updated entity from the database and verify its new sate
        RecommendationEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getAuthor());
    }
}
