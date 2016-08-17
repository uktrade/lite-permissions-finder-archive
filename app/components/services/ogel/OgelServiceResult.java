package components.services.ogel;

import java.util.List;

public class OgelServiceResult {

  public String id;

  public String title;

  public String additionalText;

  public List<String> canConditions;

  public List<String> cantConditions;

  public List<String> mustConditions;

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getAdditionalText() {
    return additionalText;
  }

  public List<String> getCanConditions() {
    return canConditions;
  }

  public List<String> getCantConditions() {
    return cantConditions;
  }

  public List<String> getMustConditions() {
    return mustConditions;
  }
}
