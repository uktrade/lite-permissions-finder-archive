package components.services.ogels.applicable;

import java.util.List;

public class ApplicableOgelServiceResult {

  public String name;

  public String id;

  public List<String> usageSummary;

  public String getName() {
    return name;
  }

  public String getId() {
    return id;
  }

  public List<String> getUsageSummary() {
    return usageSummary;
  }
}
