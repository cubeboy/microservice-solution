package se.magnus.util.exceptions;

import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import se.magnus.util.http.HttpErrorInfo;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

  @Override
  public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
    Map<String, Object> retMap = super.getErrorAttributes(request, options);
    Throwable e = getError(request);
    HttpErrorInfo eInfo = null;

    if(e instanceof NotFoundException)
      eInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, request.uri().getPath(), e.getMessage());
    else if(e instanceof NumberFormatException)
      eInfo = new HttpErrorInfo(HttpStatus.BAD_REQUEST, request.uri().getPath(), "Type mismatch.");
    else if(e instanceof InvalidInputException)
      eInfo = new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, request.uri().getPath(), e.getMessage());
    else
      eInfo = new HttpErrorInfo(HttpStatus.BAD_REQUEST, request.uri().getPath(), e.getMessage());

    if(e instanceof ResponseStatusException) {
      ResponseStatusException ex = (ResponseStatusException)e;
      eInfo = new HttpErrorInfo(ex.getStatus(), request.uri().getPath(), ex.getReason());
    }

    retMap.put("status", eInfo.getHttpStatus());
    retMap.put("message", eInfo.getMessage());
    retMap.put("path", eInfo.getPath());
    return retMap;
  }
}
