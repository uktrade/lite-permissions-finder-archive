package utils;

import org.slf4j.Logger;

public class MyLogger {

  private final Logger logger;

  public MyLogger(Logger logger) {
    this.logger = logger;
  }

  public void error(String msg) {
    logger.error(RequestMarker.marker(), msg);
  }
}
