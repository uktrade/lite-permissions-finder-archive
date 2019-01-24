package components.cms.parser.model.navigation.column;

public class ControlListEntries {
  private final String rating;
  private final Integer priority;
  private final Boolean decontrolled;

  public ControlListEntries(String rating, Integer priority, Boolean decontrolled) {
    this.rating = rating;
    this.priority = priority;
    this.decontrolled = decontrolled;
  }

  public String getRating() {
    return rating;
  }

  public Integer getPriority() {
    return priority;
  }

  public Boolean isDecontrolled() {
    return decontrolled;
  }
}
