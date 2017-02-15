package models.controlcode;

import components.services.controlcode.frontend.FrontendServiceResult;
import uk.gov.bis.lite.controlcode.api.view.FrontEndControlCodeView.FrontEndControlCodeData;

import java.util.List;
import java.util.stream.Collectors;

public class DecontrolsDisplay {
  public final String friendlyDescription;
  public final List<String> decontrols;

  public DecontrolsDisplay(FrontendServiceResult frontendServiceResult) {
    FrontEndControlCodeData controlCodeData = frontendServiceResult.getControlCodeData();
    this.friendlyDescription = controlCodeData.getFriendlyDescription();
    this.decontrols = controlCodeData.getDecontrols().stream()
        .map(decontrol -> decontrol.getText())
        .collect(Collectors.toList());
  }

}
