package models.controlcode;

import components.services.controlcode.frontend.ControlCodeData;
import components.services.controlcode.frontend.FrontendServiceResult;

public class TechnicalNotesDisplay {

  public final String title;

  public final String friendlyDescription;

  public final String controlCode;

  public final String technicalNotes;

  public TechnicalNotesDisplay(FrontendServiceResult frontendServiceResult) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    this.title = controlCodeData.title;
    this.friendlyDescription = controlCodeData.friendlyDescription;
    this.controlCode = controlCodeData.controlCode;
    this.technicalNotes = controlCodeData.technicalNotes;
  }

}
