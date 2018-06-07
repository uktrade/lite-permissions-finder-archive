package models.view.licencefinder;

import controllers.licencefinder.ResultsController;
import uk.gov.bis.lite.ogel.api.view.ApplicableOgelView;

import java.util.List;

public class OgelView {

  private String id;
  private String name;
  private Boolean alreadyRegistered;
  private List<String> usageSummary;

  public OgelView(ApplicableOgelView applicableView) {
    this.id = applicableView.getId();
    this.name = applicableView.getName();
    this.usageSummary = applicableView.getUsageSummary();
    this.alreadyRegistered = false; // set already registered as false fo default
  }

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

  public String getIsAlreadyRegisteredKey() {
    return id + ResultsController.IS_ALREADY_REGISTERED_KEY + alreadyRegistered;
  }

  public Boolean getAlreadyRegistered() {
    return alreadyRegistered;
  }

  public void setAlreadyRegistered(Boolean alreadyRegistered) {
    this.alreadyRegistered = alreadyRegistered;
  }
}
