package models.template;

public class AnalyticsSnippets {
  private final String headJs;
  private final String bodyHtml;

  public AnalyticsSnippets(String headJs, String bodyHtml) {
    this.headJs = headJs;
    this.bodyHtml = bodyHtml;
  }

  public String getHeadJs() {
    return headJs;
  }

  public String getBodyHtml() {
    return bodyHtml;
  }
}
