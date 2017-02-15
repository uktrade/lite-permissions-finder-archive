package models.controlcode;

import components.services.controlcode.frontend.FrontendServiceResult;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeSummary;
import uk.gov.bis.lite.controlcode.api.view.FrontEndControlCodeView.FrontEndControlCodeData;
import uk.gov.bis.lite.controlcode.api.view.FrontEndControlCodeView.FrontEndControlCodeData.FormattedAdditionalSpecifications;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AdditionalSpecificationsDisplay {
  public final String title;
  public final String friendlyDescription;
  public final String controlCodeAlias;
  public final ControlCodeSummary greatestAncestor;
  public final List<ControlCodeSummary> otherAncestors;
  public final boolean showGreatestAncestor;
  public final String clauseText;
  public final List<String> specifications;
  public final boolean showTechNotesQuestion;

  public AdditionalSpecificationsDisplay(FrontendServiceResult frontendServiceResult) {
    FrontEndControlCodeData controlCodeData = frontendServiceResult.getControlCodeData();
    this.title = controlCodeData.getTitle();
    this.friendlyDescription = controlCodeData.getFriendlyDescription();
    this.controlCodeAlias = controlCodeData.getAlias();
    if (frontendServiceResult.getGreatestAncestor().isPresent()) {
      this.greatestAncestor = frontendServiceResult.getGreatestAncestor().get();
      showGreatestAncestor = true;
    }
    else {
      this.greatestAncestor = null;
      showGreatestAncestor = false;
    }
    this.otherAncestors = frontendServiceResult.getOtherAncestors();
    FormattedAdditionalSpecifications additionalSpecifications = controlCodeData.getAdditionalSpecifications();
    if (additionalSpecifications != null) {
      this.clauseText = additionalSpecifications.getClauseText();
      if (additionalSpecifications.getSpecificationText() != null) {
        this.specifications = additionalSpecifications.getSpecificationText().stream().map(t -> t.getText()).collect(Collectors.toList());
      }
      else {
        this.specifications = Collections.emptyList();
      }
    }
    else {
      this.clauseText = null;
      this.specifications = Collections.emptyList();
    }
    this.showTechNotesQuestion = frontendServiceResult.canShowTechnicalNotes();
  }

}
