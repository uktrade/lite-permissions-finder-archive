package models.summary;

import components.services.controlcode.frontend.ControlCodeData;
import components.services.controlcode.frontend.FrontendServiceResult;
import components.services.ogels.ogel.OgelServiceResult;
import models.common.Country;

import java.util.List;
import java.util.stream.Collectors;

public class SummaryField {

  /**
   * Type of summary field
   */
  public final SummaryFieldType summaryFieldType;

  /**
   * The fields heading, describing the type of content to be shown
   */
  public final String heading;

  /**
   * Displays the fields data
   */
  public final String content;

  /**
   * Raw value used to drive the content, e.g. what's stored in the DAO. This is optional
   */
  public final String data;

  /**
   * URL which will place a user on a journey allowing them to alter this fields data
   */
  public final String editLinkUrl;

  /**
   * States whether the value stored in content is HTML
   */
  public final boolean isContentHtml;


  public SummaryField(SummaryFieldType summaryFieldType, String heading, String content, String data,
                      String editLinkUrl, boolean isContentHtml) {
    this.summaryFieldType = summaryFieldType;
    this.heading = heading;
    this.content = content;
    this.data = data;
    this.editLinkUrl = editLinkUrl;
    this.isContentHtml = isContentHtml;
  }

  /**
   * Creates a SummaryField from a frontend service result and an edit link URL
   * @param frontendServiceResult
   * @param editLinkUrl
   * @return a SummaryField for the control code
   */
  public static SummaryField fromFrontendServiceResult(FrontendServiceResult frontendServiceResult, String editLinkUrl) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    String content =  "<strong class=\"bold-small\">" + controlCodeData.controlCode
        + "</strong> - " + controlCodeData.title;
    return new SummaryField(SummaryFieldType.CONTROL_CODE, "Goods rating", content, null, editLinkUrl, true);
  }

  /**
   * Creates SummaryField from an OGEL service result and an edit link URL
   * @param ogelServiceResult
   * @param editLinkUrl
   * @return a Summary field for the OGEL type
   */
  public static SummaryField fromOgelServiceResult(OgelServiceResult ogelServiceResult, String editLinkUrl) {
    return new SummaryField(SummaryFieldType.OGEL_TYPE, "Licence type", ogelServiceResult.name, ogelServiceResult.id, editLinkUrl, false);
  }

  /**
   * Create a SummmaryField from a list of destination countries and an edit link URL
   * @param destinationCountries
   * @param editLinkUrl
   * @return
   */
  public static SummaryField fromDestinationCountryList(List<Country> destinationCountries, String editLinkUrl) {
    // Create a <ul> with an <li> for each country
    String content = destinationCountries.stream()
        .map(country -> "<ul>" + country.getCountryName() + "</ul>")
        .collect(Collectors.joining("", "<ul>", "</ul>"));
    return new SummaryField(SummaryFieldType.DESTINATION_COUNTRIES, "Destination countries", content, null, editLinkUrl, true);
  }

}
