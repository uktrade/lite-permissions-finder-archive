package models.controlcode;

import components.services.controlcode.frontend.Ancestor;
import components.services.controlcode.frontend.ControlCodeData;
import components.services.controlcode.frontend.FrontendServiceResult;

import java.util.List;
import java.util.Optional;

public class ControlCodeDisplay {
  public final String title;

  public final String friendlyDescription;

  public final String controlCode;

  public final Ancestor greatestAncestor;

  public final List<Ancestor> otherAncestors;

  public final boolean showGreatestAncestor;

  public ControlCodeDisplay(FrontendServiceResult frontendServiceResult) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    this.title = controlCodeData.title;
    this.friendlyDescription = controlCodeData.friendlyDescription;
    this.controlCode = controlCodeData.controlCode;
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
