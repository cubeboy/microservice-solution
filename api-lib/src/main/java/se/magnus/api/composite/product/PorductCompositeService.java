package se.magnus.api.composite.product;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;

@Api(description = "REST API for composite product information.")
public interface PorductCompositeService {
  @ApiOperation(
    value = "${api.product-composite.create-composite-product.description}",
    notes = "${api.product-composite.create-composite-product.notes}")
  @ApiResponses(value = {
    @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
    @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
  })
  @PostMapping(
    value    = "/product-composite",
    consumes = "application/json")
  void createCompositeProduct(@RequestBody ProductAggregate body);

  @ApiOperation(
    value = "${api.product-composite.get-composite-product.description}",
    notes = "${api.product-composite.get-composite-product.notes}")
  @ApiResponses(value = {
    @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
    @ApiResponse(code = 404, message = "Not found, the specified id does not exist."),
    @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fails. See response message for more information.")
  })
  @GetMapping(
    value="product-composite/{productId}",
    produces="application/json"
  )
  ProductAggregate getCompositeProduct(@PathVariable int productId);

  @ApiOperation(
    value = "${api.product-composite.delete-composite-product.description}",
    notes = "${api.product-composite.delete-composite-product.notes}")
  @ApiResponses(value = {
    @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
    @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
  })
  @DeleteMapping(value = "/product-composite/{productId}")
  void deleteCompositeProduct(@PathVariable int productId);
  
}
