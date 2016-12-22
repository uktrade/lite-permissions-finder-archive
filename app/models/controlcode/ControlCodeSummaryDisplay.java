package models.controlcode;

import components.services.controlcode.Ancestor;
import components.services.controlcode.ControlCodeData;
import components.services.controlcode.FrontendServiceResult;
import controllers.controlcode.routes;
import models.GoodsType;

import java.util.List;

public class ControlCodeSummaryDisplay {
  public final String formAction;
  public final String title;
  public final String friendlyDescription;
  public final String controlCodeAlias;
  public final Ancestor greatestAncestor;
  public final List<Ancestor> otherAncestors;
  public final boolean showGreatestAncestor;
  public final String couldDescribeItemsLabel;
  public final ControlCodeSubJourney controlCodeSubJourney;
  public final boolean decontrolsExist;
  public final boolean additionalSpecificationsExist;
  public final boolean technicalNotesExist;

  public ControlCodeSummaryDisplay(ControlCodeSubJourney controlCodeSubJourney, FrontendServiceResult frontendServiceResult) {
    ControlCodeData controlCodeData = frontendServiceResult.controlCodeData;
    this.title = controlCodeData.title;
    this.friendlyDescription = controlCodeData.friendlyDescription;
    this.controlCodeAlias = controlCodeData.alias;
    this.controlCodeSubJourney = controlCodeSubJourney;
    this.decontrolsExist = controlCodeData.canShowDecontrols();
    this.additionalSpecificationsExist = controlCodeData.canShowAdditionalSpecifications();
    this.technicalNotesExist = controlCodeData.canShowTechnicalNotes();
    if (frontendServiceResult.greatestAncestor.isPresent()) {
      this.greatestAncestor = frontendServiceResult.greatestAncestor.get();
      this.showGreatestAncestor = true;
    }
    else {
      this.greatestAncestor = null;
      this.showGreatestAncestor = false;
    }
    this.otherAncestors = frontendServiceResult.otherAncestors;
    if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH) {
      this.formAction = routes.ControlCodeSummaryController.handleSubmit().url();
      this.couldDescribeItemsLabel = "Could this describe your items?";
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_SOFTWARE) {
      this.formAction = routes.ControlCodeSummaryController.handleSearchRelatedToSubmit(GoodsType.SOFTWARE.urlString()).url();
      this.couldDescribeItemsLabel = "Could this describe the item your software is used with?";
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH_RELATED_TO_TECHNOLOGY) {
      this.formAction = routes.ControlCodeSummaryController.handleSearchRelatedToSubmit(GoodsType.TECHNOLOGY.urlString()).url();
      this.couldDescribeItemsLabel = "Could this describe the item your technology is used with?";
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.SOFTWARE_CONTROLS) {
      this.formAction = routes.ControlCodeSummaryController.handleControlsSubmit(GoodsType.SOFTWARE.urlString()).url();
      this.couldDescribeItemsLabel = "Could this describe your items?";
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.TECHNOLOGY_CONTROLS) {
      this.formAction = routes.ControlCodeSummaryController.handleControlsSubmit(GoodsType.TECHNOLOGY.urlString()).url();
      this.couldDescribeItemsLabel = "Could this describe your items?";
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      this.formAction = routes.ControlCodeSummaryController.handleRelatedControlsSubmit(GoodsType.SOFTWARE.urlString()).url();
      this.couldDescribeItemsLabel = "Could this describe your items?";
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      this.formAction = routes.ControlCodeSummaryController.handleRelatedControlsSubmit(GoodsType.TECHNOLOGY.urlString()).url();
      this.couldDescribeItemsLabel = "Could this describe your items?";
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.SOFTWARE_CATCHALL_CONTROLS) {
      this.formAction = routes.ControlCodeSummaryController.handleCatchallControlsSubmit(GoodsType.SOFTWARE.urlString()).url();
      this.couldDescribeItemsLabel = "Could this describe your items?";
    }
    else if (controlCodeSubJourney == models.controlcode.ControlCodeSubJourney.TECHNOLOGY_CATCHALL_CONTROLS) {
      this.formAction = routes.ControlCodeSummaryController.handleCatchallControlsSubmit(GoodsType.TECHNOLOGY.urlString()).url();
      this.couldDescribeItemsLabel = "Could this describe your items?";
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
  }

}
