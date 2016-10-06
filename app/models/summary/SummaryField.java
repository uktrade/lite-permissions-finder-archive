package models.summary;

import components.services.controlcode.frontend.ControlCodeData;
import components.services.controlcode.frontend.FrontendServiceResult;
import components.services.ogels.ogel.OgelServiceResult;
import models.common.Country;

import java.util.List;
import java.util.stream.Collectors;

public class SummaryField {

  public final String heading;
  public final String content;
  public final String editLinkUrl;
  public final boolean isContentHtml;

  public SummaryField(String heading, String content, String editLinkUrl, boolean isContentHtml) {
    this.heading = heading;
    this.content = content;
    this.editLinkUrl = editLinkUrl;
    this.isContentHtml = isContentHtml;
  }

  public static SummaryField fromFrontendServiceResult(FrontendServiceResult frontendServiceResult, String editLinkUrl) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    String content =  "<strong class=\"bold-small\">" + controlCodeData.controlCode
        + "</strong> - " + controlCodeData.title;
    return new SummaryField("Goods rating", content, editLinkUrl, true);
  }

  public static SummaryField fromOgelServiceResult(OgelServiceResult ogelServiceResult, String editLinkUrl) {
    return new SummaryField("Licence type", ogelServiceResult.name, editLinkUrl, false);
  }

  public static SummaryField fromDestinationCountryList(List<Country> destinationCountries, String editLinkUrl) {
    // Create a <ul> with an <li> for each country
    String content = destinationCountries.stream()
        .map(country -> "<ul>" + country.getCountryName() + "</ul>")
        .collect(Collectors.joining("", "<ul>", "</ul>"));
    return new SummaryField("Destination countries", content, editLinkUrl, true);
  }

}
