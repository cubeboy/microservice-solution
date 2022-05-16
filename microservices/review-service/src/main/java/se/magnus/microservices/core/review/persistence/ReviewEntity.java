package se.magnus.microservices.core.review.persistence;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.magnus.api.core.review.Review;

@EqualsAndHashCode
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
    name = "reviews"
  , indexes = { @Index(name = "reviews_unique_idx", unique = true, columnList = "productId,reviewId") })
public class ReviewEntity {

  @Id @GeneratedValue
  private int id;

  @Version
  private int version;

  private int productId;
  private int reviewId;
  private String author;
  private String subject;
  @EqualsAndHashCode.Exclude
  private String content;

  public ReviewEntity(Review review) {
    this.productId = review.getProductId();
    this.reviewId = review.getReviewId();
    this.author = review.getAuthor();
    this.subject = review.getSubject();
    this.content = review.getContent();
  }

  public Review toReview(String serviceAddress) {
    return Review.builder()
      .productId(this.productId)
      .reviewId(this.reviewId)
      .author(this.author)
      .subject(this.subject)
      .content(this.content).build();
  }
}
