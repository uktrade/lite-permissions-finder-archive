package models.controlcode;

import components.services.controlcode.Ancestor;
import components.services.controlcode.ControlCodeData;
import components.services.controlcode.FrontendServiceResult;

import java.util.List;

public class ControlCodeDisplay {
  public final String title;

  public final String friendlyDescription;

  public final String controlCodeAlias;

  public final Ancestor greatestAncestor;

  public final List<Ancestor> otherAncestors;

  public final boolean showGreatestAncestor;

  public ControlCodeDisplay(FrontendServiceResult frontendServiceResult) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    this.title = controlCodeData.title;
    this.friendlyDescription = controlCodeData.friendlyDescription;
    this.controlCodeAlias = controlCodeData.alias;
    if (frontendServiceResult.greatestAncestor.isPresent()) {
      this.greatestAncestor = frontendServiceResult.greatestAncestor.get();
      showGreatestAncestor = true;
    }
    else {
      this.greatestAncestor = null;
      showGreatestAncestor = false;
    }
    this.otherAncestors = frontendServiceResult.otherAncestors;
  }

}
