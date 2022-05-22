package se.magnus.microservices.core.product.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductServiceError {
  private String path;
  private String message;
}
