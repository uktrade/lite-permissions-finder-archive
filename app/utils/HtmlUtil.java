package utils;

import play.twirl.api.Html;
import play.twirl.api.HtmlFormat;

public class HtmlUtil {

  /**
   * Converts newlines in a string to p or br tags as appropriate
   *
   * @param input    The plaintext string to add paragraphs to
   * @return String  The input with single newlines converted to br tags and double newlines converted to p tags
   */
  public static Html newlinesToParagraphs(String input) {
    String output = "<p>" + HtmlFormat.escape(input) + "</p>";
    output = output.replace("\r", "");
    output = output.replace("\n", "<br>");
    output = output.replace("<br><br>", "</p><p>");
    return HtmlFormat.raw(output);
  }

}
