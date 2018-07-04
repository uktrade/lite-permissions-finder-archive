package utils;

import org.slf4j.LoggerFactory;

public class MyLoggerFactory {

  public static MyLogger getLogger(Class<?> clazz) {
    return new MyLogger(LoggerFactory.getLogger(clazz));
  }

}
