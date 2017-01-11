package models.controlcode;

import components.services.controlcode.Ancestor;
import components.services.controlcode.ControlCodeData;
import components.services.controlcode.FrontendControlCode;

import java.util.List;
import java.util.stream.Collectors;

public class DecontrolsDisplay {
  public final String title;
  public final String friendlyDescription;
  public final String controlCodeAlias;
  public final Ancestor greatestAncestor;
  public final List<Ancestor> otherAncestors;
  public final boolean showGreatestAncestor;
  public final List<String> decontrols;

  public DecontrolsDisplay(FrontendControlCode frontendControlCode) {
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
    this.decontrols = controlCodeData.decontrols.stream()
        .map(decontrol -> decontrol.text)
        .collect(Collectors.toList());
  }

}
