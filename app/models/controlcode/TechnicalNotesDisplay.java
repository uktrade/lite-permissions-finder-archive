package models.controlcode;

import components.services.controlcode.FrontendServiceResult;
import uk.gov.bis.lite.controlcode.api.view.ControlCodeSummary;
import uk.gov.bis.lite.controlcode.api.view.FrontEndControlCodeView.FrontEndControlCodeData;

import java.util.List;

public class TechnicalNotesDisplay {
  public final String title;
  public final String friendlyDescription;
  public final String controlCodeAlias;
  public final ControlCodeSummary greatestAncestor;
  public final List<ControlCodeSummary> otherAncestors;
  public final boolean showGreatestAncestor;
  public final String technicalNotes;
  public final String questionHeading;

  public TechnicalNotesDisplay(FrontendServiceResult frontendServiceResult) {
    FrontEndControlCodeData controlCodeData = frontendServiceResult.getControlCodeData();
    this.title = "Technical notes for " + controlCodeData.getControlCode();
    this.questionHeading = "Does " + controlCodeData.getControlCode() + " still describe your item?";
    this.friendlyDescription = controlCodeData.getFriendlyDescription();
    this.controlCodeAlias = controlCodeData.getAlias();
    this.technicalNotes = controlCodeData.getTechnicalNotes();
    if (frontendServiceResult.getGreatestAncestor().isPresent()) {
      this.greatestAncestor = frontendServiceResult.getGreatestAncestor().get();
      showGreatestAncestor = true;
    }
    else {
      this.greatestAncestor = null;
      showGreatestAncestor = false;
    }
    this.otherAncestors = frontendServiceResult.getOtherAncestors();
  }

}
