package models.view;

public class ProgressView {

  private final String code;
  private final String description;

  public ProgressView(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }

}
