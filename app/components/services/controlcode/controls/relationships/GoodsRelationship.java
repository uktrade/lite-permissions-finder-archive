package components.services.controlcode.controls.relationships;

import com.fasterxml.jackson.annotation.JsonProperty;
import components.services.controlcode.FrontendControlCode;
import models.GoodsType;

public class GoodsRelationship {
  public final String description;
  public final FrontendControlCode controlCode;
  public final GoodsType controlType;
  public final GoodsType relatedToControlType;
  public final String notes;
  public final String controlEntryQuestion;

  public GoodsRelationship(@JsonProperty("description") String description,
                           @JsonProperty("controlCode") FrontendControlCode controlCode,
                           @JsonProperty("controlType") GoodsType controlType,
                           @JsonProperty("relatedToControlType") GoodsType relatedToControlType,
                           @JsonProperty("notes") String notes,
                           @JsonProperty("controlEntryQuestion") String controlEntryQuestion) {
    this.description = description;
    this.controlCode = controlCode;
    this.controlType = controlType;
    this.relatedToControlType = relatedToControlType;
    this.notes = notes;
    this.controlEntryQuestion = controlEntryQuestion;
  }
}
