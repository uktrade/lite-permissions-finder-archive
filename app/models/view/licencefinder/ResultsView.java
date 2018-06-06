package models.view.licencefinder;

import java.util.List;

public class ResultsView {

  private String controlCode;
  private String destinationCountry;
  private List<OgelView> ogelViews;

  public String getTitle() {
    if (!ogelViews.isEmpty()) {
      return "Open licences available for exporting goods described in Control list entry " + controlCode + " to " + destinationCountry;
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
    return ogelViews != null && ogelViews.size() > 0;
  }
}
