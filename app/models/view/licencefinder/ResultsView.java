package models.view.licencefinder;

import java.util.ArrayList;
import java.util.List;

public class ResultsView {

  private String controlCode;
  private String destinationCountry;
  private List<OgelView> ogelViews;

  public ResultsView(String controlCode, String destinationCountry) {
    this.controlCode = controlCode;
    this.destinationCountry = destinationCountry;
    ogelViews = new ArrayList<>();
  }

  public ResultsView() {
    ogelViews = new ArrayList<>();
  }

  public String getTitle() {
    if (ogelViews != null && !ogelViews.isEmpty()) {
      return "Open licences available for exporting goods described in control list entry " + controlCode + " to " + destinationCountry;
    } else {
      return "No open licences available";
    }
  }

  public List<OgelView> getOgelViews() {
    return ogelViews;
  }

  public void setOgelViews(List<OgelView> ogelViews) {
    this.ogelViews = ogelViews;
  }

  public boolean hasOgels() {
    return ogelViews != null && !ogelViews.isEmpty();
  }

  public String getControlCode() {
    return controlCode;
  }

  public void setControlCode(String controlCode) {
    this.controlCode = controlCode;
  }

  public String getDestinationCountry() {
    return destinationCountry;
  }

  public void setDestinationCountry(String destinationCountry) {
    this.destinationCountry = destinationCountry;
  }
}
