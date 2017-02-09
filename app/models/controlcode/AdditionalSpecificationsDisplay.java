package models.controlcode;

import components.services.controlcode.AdditionalSpecifications;
import components.services.controlcode.Ancestor;
import components.services.controlcode.ControlCodeData;
import components.services.controlcode.FrontendControlCode;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AdditionalSpecificationsDisplay {
  public final String title;
  public final String friendlyDescription;
  public final String controlCodeAlias;
  public final Ancestor greatestAncestor;
  public final List<Ancestor> otherAncestors;
  public final boolean showGreatestAncestor;
  public final String clauseText;
  public final List<String> specifications;
  public final boolean showTechNotesQuestion;

  public AdditionalSpecificationsDisplay(FrontendControlCode frontendControlCode) {
    ControlCodeData controlCodeData = frontendControlCode.controlCodeData;
    this.title = controlCodeData.title;
    this.friendlyDescription = controlCodeData.friendlyDescription;
    this.controlCodeAlias = controlCodeData.alias;
    if (frontendControlCode.greatestAncestor.isPresent()) {
      this.greatestAncestor = frontendControlCode.greatestAncestor.get();
      showGreatestAncestor = true;
    }
    else {
      this.greatestAncestor = null;
      showGreatestAncestor = false;
    }
    this.otherAncestors = frontendControlCode.otherAncestors;
    AdditionalSpecifications additionalSpecifications = frontendControlCode.controlCodeData.additionalSpecifications;
    if (additionalSpecifications != null) {
      this.clauseText = additionalSpecifications.clauseText;
      if (additionalSpecifications.specificationText != null) {
        this.specifications = additionalSpecifications.specificationText.stream().map(t -> t.text).collect(Collectors.toList());
      }
      else {
        this.specifications = Collections.emptyList();
      }
    }
    else {
      this.clauseText = null;
      this.specifications = Collections.emptyList();
    }
    this.showTechNotesQuestion = controlCodeData.canShowTechnicalNotes();
  }

}
