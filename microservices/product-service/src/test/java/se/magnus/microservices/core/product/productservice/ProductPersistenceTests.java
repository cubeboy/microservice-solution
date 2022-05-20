package se.magnus.microservices.core.product.productservice;

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
import se.magnus.microservices.core.product.persistence.ProductEntity;
import se.magnus.microservices.core.product.persistence.ProductRepository;

@DataMongoTest
@ExtendWith(SpringExtension.class)
public class ProductPersistenceTests {

  @Autowired
  private ProductRepository repository;

  private ProductEntity savedEntity;

  @BeforeEach
   public void setupDb() {
     repository.deleteAll();

      ProductEntity entity = ProductEntity.builder().productId(1).name("n").weight(1).build();
      savedEntity = repository.save(entity).block();

      assertEquals(entity, savedEntity);
  }

  @Test
  public void create() {

      ProductEntity newEntity = ProductEntity.builder().productId(2).name("n").weight(2).build();
      StepVerifier.create(repository.save(newEntity))
        .expectNextMatches(createEntity -> newEntity.getProductId() == createEntity.getProductId())
        .verifyComplete();
  }

  @Test
  public void update() {
    savedEntity.setName("n2");
    StepVerifier.create(repository.save(savedEntity))
      .expectNextMatches(updatedEntity -> updatedEntity.getName().equals("n2"))
      .verifyComplete();

    StepVerifier.create(repository.findById(savedEntity.getId()))
      .expectNextMatches(foundEntity -> 
        foundEntity.getVersion() == 1&& 
        foundEntity.getName().equals("n2"))
      .verifyComplete();
  }

  @Test
  public void delete() {
    StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
    StepVerifier.create(repository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
  }

  @Test
  public void getByProductId() {
    StepVerifier.create(repository.findByProductId(savedEntity.getProductId()))
      .expectNextMatches(foundEntity -> savedEntity.equals(foundEntity))
      .verifyComplete();
  }

  @Test
  public void duplicateError() {
    ProductEntity entity = ProductEntity.builder()
      .id(savedEntity.getId())
      .productId(1)
      .name("n")
      .weight(1).build();

    StepVerifier.create(repository.save(entity))
      .expectError(DuplicateKeyException.class)
      .verify();
  }

  @Test
   	public void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        ProductEntity entity1 = repository.findById(savedEntity.getId()).block();
        ProductEntity entity2 = repository.findById(savedEntity.getId()).block();

        // Update the entity using the first entity object
        entity1.setName("n1");
        repository.save(entity1).block();

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        StepVerifier.create(repository.save(entity2)).expectError(OptimisticLockingFailureException.class).verify();

        // Get the updated entity from the database and verify its new sate
        StepVerifier.create(repository.findById(savedEntity.getId()))
          .expectNextMatches(foundEntity ->
            foundEntity.getVersion() == 1 &&
            foundEntity.getName().equals("n1"))
          .verifyComplete();
    }
}
