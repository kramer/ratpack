package org.ratpackframework.handler;

import org.ratpackframework.responder.FinalizedResponse;
import org.ratpackframework.routing.Router;
import org.ratpackframework.routing.internal.RoutedRequest;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;

import java.util.Arrays;
import java.util.Map;

public class RoutingHandler implements Handler<HttpServerRequest> {

  private final Router router;
  private ErrorHandler errorHandler;
  private Handler<HttpServerRequest> notFoundHandler;

  public RoutingHandler(Router router, ErrorHandler errorHandler, Handler<HttpServerRequest> notFoundHandler) {
    this.router = router;
    this.errorHandler = errorHandler;
    this.notFoundHandler = notFoundHandler;
  }

  @Override
  public void handle(final HttpServerRequest request) {
    request.pause();

    RoutedRequest routedRequest = new RoutedRequest(request, errorHandler, notFoundHandler, errorHandler.asyncHandler(request, new Handler<FinalizedResponse>() {
      @Override
      public void handle(FinalizedResponse response) {
        HttpServerResponse realResponse = request.response;

        realResponse.statusCode = response.getStatus();

        for (Map.Entry<String, Object> entry : response.getHeaders().entrySet()) {
          Object value = entry.getValue();
          @SuppressWarnings("unchecked")
          Iterable<Object> values = value instanceof Iterable ? (Iterable<Object>) value : Arrays.asList(value);
          for (Object singleValue : values) {
            realResponse.putHeader(entry.getKey(), singleValue);
          }
        }
        realResponse.end(response.getBuffer());
      }
    }));

    router.handle(routedRequest);
  }

}