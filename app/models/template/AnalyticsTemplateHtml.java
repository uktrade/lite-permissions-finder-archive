package models.template;

public class AnalyticsTemplateHtml {
  private final String head;
  private final String body;

  public AnalyticsTemplateHtml(String head, String body) {
    this.head = head;
    this.body = body;
  }

  public String getHead() {
    return head;
  }

  public String getBody() {
    return body;
  }
}
