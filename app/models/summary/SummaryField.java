package models.summary;

import components.services.controlcode.frontend.FrontendServiceResult;
import models.common.Country;
import uk.gov.bis.lite.controlcode.api.view.FrontEndControlCodeView.FrontEndControlCodeData;
import uk.gov.bis.lite.ogel.api.view.OgelFullView;

import java.util.List;
import java.util.stream.Collectors;

public class SummaryField {

  /**
   * Type of summary field
   */
  private final SummaryFieldType summaryFieldType;

  /**
   * The fields heading, describing the type of content to be shown
   */
  private final String heading;

  /**
   * Displays the fields data
   */
  private final String content;

  /**
   * Raw value used to drive the content, e.g. what's stored in the DAO. This is optional
   */
  private final String data;

  /**
   * URL which will place a user on a journey allowing them to alter this fields data
   */
  private final String editLinkUrl;

  /**
   * States whether the value stored in content is HTML
   */
  private final boolean isContentHtml;


  /**
   * State whether the data associated with this field is valid
   */
  private final boolean isValid;

  public SummaryField(SummaryFieldType summaryFieldType, String heading, String content, String data,
                      String editLinkUrl, boolean isContentHtml, boolean isValid) {
    this.summaryFieldType = summaryFieldType;
    this.heading = heading;
    this.content = content;
    this.data = data;
    this.editLinkUrl = editLinkUrl;
    this.isContentHtml = isContentHtml;
    this.isValid = isValid;
  }

  /**
   * Creates a SummaryField from a frontend service result and an edit link URL
   * @param frontendServiceResult The frontend control code service result
   * @param editLinkUrl The edit link URL for the OGEL ID
   * @return a SummaryField for the control code
   */
  public static SummaryField fromFrontendServiceResult(FrontendServiceResult frontendServiceResult, String editLinkUrl) {
    FrontEndControlCodeData controlCodeData = frontendServiceResult.getControlCodeData();
    String content =  "<strong class=\"bold-small\">" + controlCodeData.getControlCode()
        + "</strong> - " + controlCodeData.getTitle();
    return new SummaryField(SummaryFieldType.CONTROL_CODE, "Classification", content, null, editLinkUrl, true, true);
  }

  /**
   * Creates SummaryField from an OGEL service result and an edit link URL
   * @param ogelServiceResult The OGEL service result
   * @param editLinkUrl The edit link URL for the OGEL ID
   * @param isValid Is the OGEL ID associated with this field valid?
   * @return a Summary field for the OGEL type
   */
  public static SummaryField fromOgelServiceResult(OgelFullView ogelServiceResult,
                                                   String editLinkUrl, boolean isValid) {
    return new SummaryField(SummaryFieldType.OGEL_TYPE, "Licence", ogelServiceResult.getName(), ogelServiceResult.getId(),
        editLinkUrl, false, isValid);
  }

  /**
   * Create a SummmaryField from a list of destination countries and an edit link URL
   * @param destinationCountries The destination countries list
   * @param editLinkUrl The edit URL for the destination countries
   * @return a SummaryField for the destination countries
   */
  public static SummaryField fromDestinationCountryList(List<Country> destinationCountries, String editLinkUrl) {
    // Create a <ul> with an <li> for each country
    String content = destinationCountries.stream()
        .map(country -> "<li>" + country.getCountryName() + "</li>")
        .collect(Collectors.joining("", "<ul>", "</ul>"));
    return new SummaryField(SummaryFieldType.DESTINATION_COUNTRIES, "Destination(s)", content, null,
        editLinkUrl, true, true);
  }

  public SummaryFieldType getSummaryFieldType() {
    return summaryFieldType;
  }

  public String getHeading() {
    return heading;
  }

  public String getContent() {
    return content;
  }

  public String getData() {
    return data;
  }

  public String getEditLinkUrl() {
    return editLinkUrl;
  }

  public boolean isContentHtml() {
    return isContentHtml;
  }

  public boolean isValid() {
    return isValid;
  }
}
