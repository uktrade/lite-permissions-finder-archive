package models.view;

public class BreadcrumbItemView {

  private final String text;
  private final String url;

  public BreadcrumbItemView(String text, String url) {
    this.text = text;
    this.url = url;
  }

  public String getText() {
    return text;
  }

  public String getUrl() {
    return url;
  }

}
