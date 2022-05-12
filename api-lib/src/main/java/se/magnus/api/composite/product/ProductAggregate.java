package se.magnus.api.composite.product;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductAggregate {
  private final int productId;
  private final String name;
  private final int weight;
  private final List<RecommendationSummary> recommendations;
  private final List<ReviewSummary> reviews;
  private final ServiceAddresses serviceAddresses;
}
