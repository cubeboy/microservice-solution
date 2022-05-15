package se.magnus.microservices.core.recommendation.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection="recommendations")
@CompoundIndex(name = "prod-rec-id", unique = true, def = "{'productId': 1, 'recommendationId' : 1}")
public class RecommendationEntity {

  @Id
  private String id;

  @Version
  private Integer version;

  private int productId;
  private int recommendationId;
  private String author;
  private int rating;
  @EqualsAndHashCode.Exclude
  private String content;
}
