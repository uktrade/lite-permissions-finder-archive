package models.controlcode;

import components.services.controlcode.Ancestor;
import components.services.controlcode.ControlCodeData;
import components.services.controlcode.FrontendServiceResult;
import controllers.controlcode.routes;
import models.GoodsType;

import java.util.List;

public class ControlCodeDisplay {
  public final String formAction;
  public final String title;
  public final String friendlyDescription;
  public final String controlCodeAlias;
  public final Ancestor greatestAncestor;
  public final List<Ancestor> otherAncestors;
  public final boolean showGreatestAncestor;
  public final String couldDescribeItemsLabel;
  public final ControlCodeJourney controlCodeJourney;
  public final boolean showPickAgain;

  public ControlCodeDisplay(ControlCodeJourney controlCodeJourney, FrontendServiceResult frontendServiceResult, boolean canPickFromResultsAgain) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    this.title = controlCodeData.title;
    this.friendlyDescription = controlCodeData.friendlyDescription;
    this.controlCodeAlias = controlCodeData.alias;
    this.controlCodeJourney = controlCodeJourney;
    this.showPickAgain = canPickFromResultsAgain;
    if (frontendServiceResult.greatestAncestor.isPresent()) {
      this.greatestAncestor = frontendServiceResult.greatestAncestor.get();
      showGreatestAncestor = true;
    }
    else {
      this.greatestAncestor = null;
      showGreatestAncestor = false;
    }
    this.otherAncestors = frontendServiceResult.otherAncestors;
    if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH) {
      this.formAction = routes.ControlCodeController.handleSearchSubmit().url();
      this.couldDescribeItemsLabel = "Could this describe your items?";
    }
    else if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      this.formAction = routes.ControlCodeController.handleSearchRelatedToSubmit(GoodsType.SOFTWARE.toUrlString()).url();
      this.couldDescribeItemsLabel = "Could this describe the item your software is used with?";
    }
    else if (controlCodeJourney == ControlCodeJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      this.formAction = routes.ControlCodeController.handleSearchRelatedToSubmit(GoodsType.TECHNOLOGY.toUrlString()).url();
      this.couldDescribeItemsLabel = "Could this describe the item your technology is used with?";
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS) {
      this.formAction = routes.ControlCodeController.handleControlsSubmit(GoodsType.SOFTWARE.toUrlString()).url();
      this.couldDescribeItemsLabel = "Could this describe your items?";
    }
    else if (controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CONTROLS) {
      this.formAction = routes.ControlCodeController.handleControlsSubmit(GoodsType.TECHNOLOGY.toUrlString()).url();
      this.couldDescribeItemsLabel = "Could this describe your items?";
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      this.formAction = routes.ControlCodeController.handleRelatedControlsSubmit(GoodsType.SOFTWARE.toUrlString()).url();
      this.couldDescribeItemsLabel = "Could this describe your items?";
    }
    else if (controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      this.formAction = routes.ControlCodeController.handleRelatedControlsSubmit(GoodsType.TECHNOLOGY.toUrlString()).url();
      this.couldDescribeItemsLabel = "Could this describe your items?";
    }
    else if (controlCodeJourney == ControlCodeJourney.SOFTWARE_CATCHALL_CONTROLS) {
      this.formAction = routes.ControlCodeController.handleCatchallControlsSubmit(GoodsType.SOFTWARE.toUrlString()).url();
      this.couldDescribeItemsLabel = "Could this describe your items?";
    }
    else if (controlCodeJourney == ControlCodeJourney.TECHNOLOGY_CATCHALL_CONTROLS) {
      this.formAction = routes.ControlCodeController.handleCatchallControlsSubmit(GoodsType.TECHNOLOGY.toUrlString()).url();
      this.couldDescribeItemsLabel = "Could this describe your items?";
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeJourney enum: \"%s\""
          , controlCodeJourney.toString()));
    }
  }

}
