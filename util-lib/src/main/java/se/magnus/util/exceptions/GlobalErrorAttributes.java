package se.magnus.util.exceptions;

import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import lombok.extern.slf4j.Slf4j;
import se.magnus.util.http.HttpErrorInfo;

@Slf4j
//@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

  @Override
  public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
    Map<String, Object> retMap = super.getErrorAttributes(request, options);
    Throwable e = getError(request);
    HttpErrorInfo eInfo = null;

    if(e instanceof InvalidInputException) {

    }else if(e instanceof ResponseStatusException) {
      ResponseStatusException ex = (ResponseStatusException)e;
      eInfo = new HttpErrorInfo(ex.getStatus(), request.uri().getPath(), ex.getReason());
    } else {
      eInfo = new HttpErrorInfo(HttpStatus.INTERNAL_SERVER_ERROR, request.uri().getPath(), e.getMessage());
    }
    log.error("GlobalErrorAttributes :: " + e.getMessage());
    retMap.put("status", eInfo.getHttpStatus());
    retMap.put("message", eInfo.getMessage());
    retMap.put("path", eInfo.getPath());
    return retMap;
  }
}
