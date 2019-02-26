package utils;

public class StringUtils {

  public static String pascalCaseText(String text) {
    text = text.toUpperCase();
    text = text.replace(" ", "_");
    text = text.replace("-", "_");
    return text;
  }
}
