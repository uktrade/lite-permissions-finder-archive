package components.services.ogels.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import models.summary.SummaryField;

public class EditSummaryField {

  @JsonProperty("fieldName")
  public final String fieldName;

  @JsonProperty("fieldValue")
  public final String fieldValue;

  @JsonProperty("editLink")
  public final String editLink;

  public EditSummaryField(String fieldName, String fieldValue, String editLink) {
    this.fieldName = fieldName;
    this.fieldValue = fieldValue;
    this.editLink = editLink;
  }

  public static EditSummaryField buildEditSummaryField (SummaryField summaryField) {
    return new EditSummaryField(summaryField.heading, summaryField.content, summaryField.editLinkUrl);
  }

}
