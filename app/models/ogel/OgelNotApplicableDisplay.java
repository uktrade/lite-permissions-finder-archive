package models.ogel;

import components.services.ogels.ogel.OgelServiceResult;

public class OgelNotApplicableDisplay {
  public final String pageTitle;
  public final String ogelTitle;
  public final String controlCode;

  public OgelNotApplicableDisplay(OgelServiceResult ogelServiceResult, String controlCode) {
    this.pageTitle = "Not all items within the classification " + controlCode + " can be exported using " + ogelServiceResult.name;
    this.ogelTitle = ogelServiceResult.name;
    this.controlCode = controlCode;
  }
}
