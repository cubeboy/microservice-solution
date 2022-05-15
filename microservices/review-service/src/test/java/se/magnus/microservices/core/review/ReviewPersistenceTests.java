package se.magnus.microservices.core.review;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import se.magnus.microservices.core.review.persistence.ReviewEntity;
import se.magnus.microservices.core.review.persistence.ReviewRepository;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ReviewPersistenceTests {
  @Autowired
  private ReviewRepository repository;

  private ReviewEntity savedEntity;

  @BeforeEach
  public void setupDb() {
    repository.deleteAll();

    ReviewEntity entity = ReviewEntity.builder()
      .productId(1)
      .reviewId(2)
      .author("a")
      .subject("s")
      .content("c").build();
    savedEntity = repository.save(entity);

    assertEquals(entity, savedEntity);
  }

  @Test
  public void create() {

    ReviewEntity newEntity = ReviewEntity.builder()
      .productId(1)
      .reviewId(3)
      .author("a")
      .subject("s")
      .content("c").build();
    repository.save(newEntity);

    ReviewEntity foundEntity = repository.findById(newEntity.getId()).get();
    assertEquals(newEntity, foundEntity);

    assertEquals(2, repository.count());
  }

  @Test
  public void update() {
    savedEntity.setAuthor("a2");
    repository.save(savedEntity);

    ReviewEntity foundEntity = repository.findById(savedEntity.getId()).get();
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
        List<ReviewEntity> entityList = repository.findByProductId(savedEntity.getProductId());

        assertEquals(1, entityList.size());
        assertEquals(savedEntity, entityList.get(0));
    }

    @Test
   	public void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        ReviewEntity entity1 = repository.findById(savedEntity.getId()).get();
        ReviewEntity entity2 = repository.findById(savedEntity.getId()).get();

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
        ReviewEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getAuthor());
    }
}
