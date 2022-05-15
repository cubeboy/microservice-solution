package se.magnus.microservices.core.product.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.magnus.api.core.product.Product;

@EqualsAndHashCode
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection="products")
public class ProductEntity {

  @Id
  private String id;

  @Version
  private Integer version;

  @Indexed(unique = true, background = true)
  private int productId;

  private String name;
  private int weight;

  public ProductEntity(Product product) {
    this.productId = product.getProductId();
    this.name = product.getName();
    this.weight = product.getWeight();
  }

  public Product toProduct(String serviceAddress) {
    return Product.builder()
      .productId(this.productId)
      .name(this.name)
      .weight(this.weight)
      .serviceAddress(serviceAddress)
      .build();
  }
}
