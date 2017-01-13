package models.ogel;

import uk.gov.bis.lite.ogel.api.view.OgelFullView;

public class OgelNotApplicableDisplay {
  public final String pageTitle;
  public final String ogelTitle;
  public final String controlCode;

  public OgelNotApplicableDisplay(OgelFullView ogelServiceResult, String controlCode) {
    this.pageTitle = "Not all items within the classification " + controlCode + " can be exported using " + ogelServiceResult.getName();
    this.ogelTitle = ogelServiceResult.getName();
    this.controlCode = controlCode;
  }
}
