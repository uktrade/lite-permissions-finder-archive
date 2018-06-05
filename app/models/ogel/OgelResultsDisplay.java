package models.ogel;

import uk.gov.bis.lite.controlcode.api.view.FrontEndControlCodeView;
import uk.gov.bis.lite.ogel.api.view.ApplicableOgelView;

import java.util.List;
import java.util.stream.Collectors;

public class OgelResultsDisplay {
  public final String pageTitle;
  public final List<ApplicableOgelView> ogels;
  public final String controlCodeTitle;
  public final String destinationCountryNamesHtml;

  public OgelResultsDisplay(List<ApplicableOgelView> ogels, FrontEndControlCodeView frontEndControlCodeView, List<String> countryNames, String controlCode, String destinationCountry) {
    this.ogels = ogels;
    if (!ogels.isEmpty()) {
      this.pageTitle = "Open licences available for exporting goods described in Control list entry " + controlCode + " to " + destinationCountry;
    } else {
      this.pageTitle = "No open licences available";
    }


    this.controlCodeTitle = frontEndControlCodeView.getControlCodeData().getTitle();
    // Creates a string in the form "A, B and C"
    if (countryNames != null && !countryNames.isEmpty()) {
      this.destinationCountryNamesHtml = countryNames.size() == 1
          ? countryNames.get(0)
          : countryNames.subList(0, countryNames.size() -1).stream()
          .collect(Collectors.joining(", ", "", " "))
          .concat("and " + countryNames.get(countryNames.size() -1));
    }
    else {
      this.destinationCountryNamesHtml = "";
    }
  }
}
