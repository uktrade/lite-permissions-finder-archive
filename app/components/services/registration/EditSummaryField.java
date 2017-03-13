package components.services.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import models.summary.SummaryField;

public class EditSummaryField {

  @JsonProperty("fieldName")
  public final String fieldName;

  @JsonProperty("fieldValue")
  public final String fieldValue;

  @JsonProperty("isHtml")
  public final boolean isHtml;

  @JsonProperty("isValid")
  public final boolean isValid;

  @JsonProperty("editLink")
  public final String editLink;

  public EditSummaryField(String fieldName, String fieldValue, boolean isHtml, boolean isValid, String editLink) {
    this.fieldName = fieldName;
    this.fieldValue = fieldValue;
    this.isHtml = isHtml;
    this.isValid = isValid;
    this.editLink = editLink;
  }

  public static EditSummaryField buildEditSummaryField (SummaryField summaryField) {
    return new EditSummaryField(summaryField.getHeading(), summaryField.getContent(), summaryField.isContentHtml(),
        summaryField.isValid(), summaryField.getEditLinkUrl());
  }

}
