package components.services.controlcode.frontend;

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

  public boolean canShowDecontrols() {
    return !(decontrols == null || decontrols.isEmpty());
  }

  public boolean canShowAdditionalSpecifications() {
    return additionalSpecifications != null && additionalSpecifications.canShow();
  }

  public boolean canShowTechnicalNotes() {
    return !(technicalNotes == null || technicalNotes.isEmpty());
  }

  public boolean canShow() {
    return canShowDecontrols() || canShowAdditionalSpecifications() || canShowTechnicalNotes();
  }

}
