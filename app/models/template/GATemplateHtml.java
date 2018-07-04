package models.template;

public class GATemplateHtml {
  private final String head;
  private final String body;

  public GATemplateHtml(String head, String body) {
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
