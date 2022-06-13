package se.magnus.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ServiceAddresses {
  private String compositeProduct;
  private String product;
  private String recommendation;
  private String review;

  public ServiceAddresses() {
    compositeProduct = null;
    product = null;
    recommendation = null;
    review = null;
  }
}
