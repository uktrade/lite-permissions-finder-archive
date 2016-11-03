package models.controlcode;

import components.services.controlcode.ControlCodeData;
import components.services.controlcode.FrontendServiceResult;

public class TechnicalNotesDisplay {

  public final String title;

  public final String friendlyDescription;

  public final String controlCodeAlias;

  public final String technicalNotes;

  public TechnicalNotesDisplay(FrontendServiceResult frontendServiceResult) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    this.title = controlCodeData.title;
    this.friendlyDescription = controlCodeData.friendlyDescription;
    this.controlCodeAlias = controlCodeData.alias;
    this.technicalNotes = controlCodeData.technicalNotes;
  }

}
