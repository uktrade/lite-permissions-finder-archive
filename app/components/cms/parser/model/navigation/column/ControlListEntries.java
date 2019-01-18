package components.cms.parser.model.navigation.column;

public class ControlListEntries {
  private final String rating;
  private final Integer priority;
  private final Boolean isDecontrolled;

  public ControlListEntries(String rating, Integer priority, Boolean isDecontrolled) {
    this.rating = rating;
    this.priority = priority;
    this.isDecontrolled = isDecontrolled;
  }

  public String getRating() {
    return rating;
  }

  public Integer getPriority() {
    return priority;
  }

  public Boolean getIsDecontrolled() {
    return isDecontrolled;
  }
}
