package se.magnus.util.http;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;

public class GlobalReactiveExceptionHandler {
  public static Mono<ServerResponse> monoException(ServerRequest request, Throwable e) {

    HttpErrorInfo eInfo = null;

    if(e instanceof NotFoundException)
      eInfo = new HttpErrorInfo(HttpStatus.NOT_FOUND, request.uri().getPath(), e.getMessage());
    else if(e instanceof NumberFormatException)
      eInfo = new HttpErrorInfo(HttpStatus.BAD_REQUEST, request.uri().getPath(), "Type mismatch.");
    else if(e instanceof InvalidInputException)
      eInfo = new HttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, request.uri().getPath(), e.getMessage());
    else
      eInfo = new HttpErrorInfo(HttpStatus.BAD_REQUEST, request.uri().getPath(), e.getMessage());

    return ServerResponse.status(eInfo.getStatus()).body(Mono.just(eInfo), HttpErrorInfo.class);
  }
}
