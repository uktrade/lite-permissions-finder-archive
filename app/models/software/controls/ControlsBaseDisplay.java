package models.software.controls;

import components.services.controlcode.category.controls.ControlCode;

import java.util.List;

public class ControlsBaseDisplay {

  public List<ControlCode> controlCodes;

  public ControlsBaseDisplay(List<ControlCode> controlCodes) {
    this.controlCodes = controlCodes;
  }
}
