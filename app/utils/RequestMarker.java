package utils;

import static net.logstash.logback.marker.Markers.append;

import components.common.logging.CorrelationId;
import org.slf4j.Marker;

public class RequestMarker {

  public static Marker marker() {
    try {
      return append("request", CorrelationId.get());
    } catch (Exception exception) {
      return append("request", "");
    }
  }

}
