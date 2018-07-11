package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

public class RequestUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(RequestUtil.class);

  public static boolean hasError(WSResponse response, Throwable throwable) {
    return throwable != null || response.getStatus() != 200;
  }

  public static void logError(WSRequest request, WSResponse response, Throwable throwable, String message) {
    if (throwable != null) {
      LOGGER.error("Unable to execute request with path {}. {},", request.getUrl(), message, throwable);
    } else if (response.getStatus() != 200) {
      LOGGER.error("Unable to execute request with path {}. Unexpected status code {} with body {}. {}",
          request.getUrl(), response.getStatus(), response.getBody(), message);
    }
  }

}
