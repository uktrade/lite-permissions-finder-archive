package models.controlcode;

import components.services.controlcode.Ancestor;
import components.services.controlcode.ControlCodeData;
import components.services.controlcode.FrontendControlCode;

import java.util.List;

public class TechnicalNotesDisplay {
  public final String title;
  public final String friendlyDescription;
  public final String controlCodeAlias;
  public final Ancestor greatestAncestor;
  public final List<Ancestor> otherAncestors;
  public final boolean showGreatestAncestor;
  public final String technicalNotes;
  public final String questionHeading;

  public TechnicalNotesDisplay(FrontendControlCode frontendControlCode) {
    ControlCodeData controlCodeData = frontendControlCode.controlCodeData;
    this.title = "Technical notes for " + controlCodeData.controlCode;
    this.questionHeading = "Does " + controlCodeData.controlCode + " still describe your item?";
    this.friendlyDescription = controlCodeData.friendlyDescription;
    this.controlCodeAlias = controlCodeData.alias;
    this.technicalNotes = controlCodeData.technicalNotes;
    if (frontendControlCode.greatestAncestor.isPresent()) {
      this.greatestAncestor = frontendControlCode.greatestAncestor.get();
      showGreatestAncestor = true;
    }
    else {
      this.greatestAncestor = null;
      showGreatestAncestor = false;
    }
    this.otherAncestors = frontendControlCode.otherAncestors;
  }

}
