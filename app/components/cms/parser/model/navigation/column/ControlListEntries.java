package components.cms.parser.model.navigation.column;

public class ControlListEntries {
  private final String rating;
  private final Integer priority;

  public ControlListEntries(String rating, Integer priority) {
    this.rating = rating;
    this.priority = priority;
  }

  public String getRating() {
    return rating;
  }

  public Integer getPriority() {
    return priority;
  }
}
