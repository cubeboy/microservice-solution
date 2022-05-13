package se.magnus.microservices.core.recommendation.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.http.ServiceUtil;

@Slf4j
@RequiredArgsConstructor
@RestController
public class RecommendationServiceImpl implements RecommendationService {
  private final ServiceUtil serviceUtil;
  @Override
  public List<Recommendation> getRecommendations(int productId) {
    if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

      if (productId == 113) {
        log.debug("No recommendations found for productId: {}", productId);
        return  new ArrayList<>();
      }

      List<Recommendation> list = new ArrayList<>();
      list.add(new Recommendation(productId, 1, "Author 1", 1, "Content 1", serviceUtil.getServiceAddress()));
      list.add(new Recommendation(productId, 2, "Author 2", 2, "Content 2", serviceUtil.getServiceAddress()));
      list.add(new Recommendation(productId, 3, "Author 3", 3, "Content 3", serviceUtil.getServiceAddress()));
      log.debug("/recommendation response size: {}", list.size());

      return list;
  }
}