package models.view.licencefinder;

import java.util.List;

public class OgelView {

  private String id;
  private String name;
  private Boolean alreadyRegistered;
  private List<String> usageSummary;

  public OgelView() {
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getUsageSummary() {
    return this.usageSummary;
  }

  public void setUsageSummary(List<String> usageSummary) {
    this.usageSummary = usageSummary;
  }

  public Boolean getAlreadyRegistered() {
    return alreadyRegistered;
  }

  public void setAlreadyRegistered(Boolean alreadyRegistered) {
    this.alreadyRegistered = alreadyRegistered;
  }
}
