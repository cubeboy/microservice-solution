package se.magnus.microservices.core.recommendation.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.microservices.core.recommendation.persistence.RecommendationEntity;
import se.magnus.microservices.core.recommendation.persistence.RecommendationRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;

@Slf4j
@RequiredArgsConstructor
@RestController
public class RecommendationServiceImpl implements RecommendationService {
  private final RecommendationRepository repository;
  private final ServiceUtil serviceUtil;

  @Override
  public Recommendation createRecommendation(Recommendation body) {
    try {
      RecommendationEntity entity = new RecommendationEntity(body);
      RecommendationEntity newEntity = repository.save(entity);

      log.debug("createRecommendation: created a recommendation entity: {}/{}", body.getProductId(), body.getRecommendationId());
      return newEntity.toRecommendation(serviceUtil.getServiceAddress());

    } catch (DuplicateKeyException dke) {
      throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId());
    }
  }

  @Override
  public List<Recommendation> getRecommendations(int productId) {

      if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

      List<RecommendationEntity> entityList = repository.findByProductId(productId);
      List<Recommendation> list = entityList.stream()
        .map( entity -> entity.toRecommendation(serviceUtil.getServiceAddress()))
        .collect(Collectors.toList());

      log.debug("getRecommendations: response size: {}", list.size());

      return list;
  }

  @Override
  public void deleteRecommendations(int productId) {
    log.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
    repository.deleteAll(repository.findByProductId(productId));
  }
}
