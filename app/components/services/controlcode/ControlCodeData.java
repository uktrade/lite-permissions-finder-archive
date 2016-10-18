package components.services.controlcode;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ControlCodeData {

  public final List<Decontrol> decontrols;

  public final String controlCode;

  public final String title;

  public final String technicalNotes;

  public final String friendlyDescription;

  public final AdditionalSpecifications additionalSpecifications;

  public ControlCodeData(@JsonProperty("decontrols") List<Decontrol> decontrols,
                         @JsonProperty("controlCode") String controlCode,
                         @JsonProperty("title") String title,
                         @JsonProperty("technicalNotes") String technicalNotes,
                         @JsonProperty("friendlyDescription") String friendlyDescription,
                         @JsonProperty("additionalSpecifications") AdditionalSpecifications additionalSpecifications) {
    this.decontrols = decontrols;
    this.controlCode = controlCode;
    this.title = title;
    this.technicalNotes = technicalNotes;
    this.friendlyDescription = friendlyDescription;
    this.additionalSpecifications = additionalSpecifications;
  }

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
