package models.controlcode;

import components.services.controlcode.frontend.FrontendServiceResult;

public class DecontrolledItemDisplay {

  public final String title;

  public final String controlCode;

  public final boolean showFirearmsOrMilitary;

  public DecontrolledItemDisplay(FrontendServiceResult frontendServiceResult, boolean isFirearmsOrMilitary) {
    this.title = frontendServiceResult.controlCodeData.friendlyDescription;
    this.controlCode = frontendServiceResult.controlCodeData.controlCode;
    this.showFirearmsOrMilitary = isFirearmsOrMilitary;
  }

}
