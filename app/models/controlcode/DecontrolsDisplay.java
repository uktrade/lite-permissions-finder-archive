package models.controlcode;

import components.services.controlcode.ControlCodeData;
import components.services.controlcode.FrontendControlCode;

import java.util.List;
import java.util.stream.Collectors;

public class DecontrolsDisplay {
  public final String friendlyDescription;
  public final List<String> decontrols;

  public DecontrolsDisplay(FrontendControlCode frontendControlCode) {
    ControlCodeData controlCodeData = frontendControlCode.controlCodeData;
    this.friendlyDescription = controlCodeData.friendlyDescription;
    this.decontrols = controlCodeData.decontrols.stream()
        .map(decontrol -> decontrol.text)
        .collect(Collectors.toList());
  }

}
