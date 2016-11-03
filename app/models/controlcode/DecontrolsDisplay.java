package models.controlcode;

import components.services.controlcode.ControlCodeData;
import components.services.controlcode.FrontendServiceResult;

import java.util.List;
import java.util.stream.Collectors;

public class DecontrolsDisplay {

  public final String title;

  public final String friendlyDescription;

  public final String controlCodeAlias;

  public final List<String> decontrols;

  public DecontrolsDisplay(FrontendServiceResult frontendServiceResult) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    this.title = controlCodeData.title;
    this.friendlyDescription = controlCodeData.friendlyDescription;
    this.controlCodeAlias = controlCodeData.alias;
    this.decontrols = controlCodeData.decontrols.stream()
        .map(decontrol -> decontrol.text)
        .collect(Collectors.toList());
  }

}
