package models.summary;

import components.services.controlcode.frontend.ControlCodeData;
import components.services.controlcode.frontend.FrontendServiceResult;
import components.services.ogels.ogel.OgelServiceResult;
import models.common.Country;

import java.util.List;
import java.util.stream.Collectors;

public class SummaryField {

  private final String content;

  private final boolean isContentHtml;

  private final SummaryFieldType summaryFieldType;

  public SummaryField(SummaryFieldType summaryFieldType, String content, boolean isContentHtml) {
    this.summaryFieldType = summaryFieldType;
    this.content = content;
    this.isContentHtml = isContentHtml;
  }

  public String getFieldValue() {
    return summaryFieldType.fieldValue;
  }

  public String getHeading() {
    return this.summaryFieldType.heading;
  }

  public String getContent() {
    return this.content;
  }

  public boolean getIsContentHtml() {
    return this.isContentHtml;
  }

  public SummaryFieldType getSummaryFieldType() {
    return this.summaryFieldType;
  }

  public static SummaryField fromFrontendServiceResult(FrontendServiceResult frontendServiceResult) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    String content =  "<strong class=\"bold-small\">" + controlCodeData.controlCode
        + "</strong> - " + controlCodeData.title;
    return new SummaryField(SummaryFieldType.CONTROL_CODE, content, true);
  }

  public static SummaryField fromOgelServiceResult(OgelServiceResult ogelServiceResult) {
    return new SummaryField(SummaryFieldType.OGEL_TYPE, ogelServiceResult.name, false);
  }

  public static SummaryField fromDestinationCountryList(List<Country> destinationCountries) {
    // Create a <ul> with an <li> for each country
    String content = destinationCountries.stream()
        .map(country -> "<ul>" + country.getCountryName() + "</ul>")
        .collect(Collectors.joining("", "<ul>", "</ul>"));
    return new SummaryField(SummaryFieldType.DESTINATION_COUNTRIES, content, true);
  }

}
