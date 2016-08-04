package controllers.services.controlcode.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ControlCodeData {

  @JsonProperty("decontrols")
  public List<Decontrol> decontrols;

  @JsonProperty("controlCode")
  public String controlCode;

  @JsonProperty("title")
  public String title;

  @JsonProperty("technicalNotes")
  public String technicalNotes;

  @JsonProperty("friendlyDescription")
  public String friendlyDescription;

  @JsonProperty("additionalSpecifications")
  public AdditionalSpecifications additionalSpecifications;

  public ControlCodeData(){}

}
