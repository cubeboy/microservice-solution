package se.magnus.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummary {
  private int reviewId;
  private String author;
  private String subject;
  private String content;
}
