package models.ogel;

import uk.gov.bis.lite.ogel.api.view.OgelFullView;

public class OgelNotApplicableDisplay {
  public final String pageTitle;
  public final String ogelTitle;
  public final String controlCode;

  public OgelNotApplicableDisplay(OgelFullView ogelServiceResult, String controlCode) {
    this.pageTitle = "You cannot use this licence";
    this.ogelTitle = ogelServiceResult.getName();
    this.controlCode = controlCode;
  }
}
