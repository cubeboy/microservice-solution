package se.magnus.api.composite.product;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductAggregate {
  private int productId;
  private String name;
  private int weight;
  private List<RecommendationSummary> recommendations = new ArrayList<RecommendationSummary>();
  private List<ReviewSummary> reviews = new ArrayList<ReviewSummary>();
  private ServiceAddresses serviceAddresses;
}
