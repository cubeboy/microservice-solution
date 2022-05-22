package se.magnus.api.core.product;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public enum ProductServiceUri {
  product ("/product");
  private final String uri;
}
