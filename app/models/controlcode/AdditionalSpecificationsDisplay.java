package models.controlcode;

import components.services.controlcode.AdditionalSpecifications;
import components.services.controlcode.Ancestor;
import components.services.controlcode.ControlCodeData;
import components.services.controlcode.FrontendServiceResult;
import controllers.controlcode.routes;
import models.GoodsType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AdditionalSpecificationsDisplay {
  public final String formAction;
  public final String title;
  public final String friendlyDescription;
  public final String controlCodeAlias;
  public final Ancestor greatestAncestor;
  public final List<Ancestor> otherAncestors;
  public final boolean showGreatestAncestor;
  public final String clauseText;
  public final List<String> specifications;

  public AdditionalSpecificationsDisplay(ControlCodeSubJourney controlCodeSubJourney, FrontendServiceResult frontendServiceResult) {
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
    AdditionalSpecifications additionalSpecifications = frontendServiceResult.controlCodeData.additionalSpecifications;
    if (additionalSpecifications != null) {
      this.clauseText = additionalSpecifications.clauseText;
      if (additionalSpecifications.specificationText != null) {
        this.specifications = additionalSpecifications.specificationText.stream().map(t -> t.text).collect(Collectors.toList());
      }
      else {
        this.specifications = Collections.emptyList();
      }
    }
    else {
      this.clauseText = null;
      this.specifications = Collections.emptyList();
    }
    if (controlCodeSubJourney.isPhysicalGoodsSearchVariant()) {
      if (controlCodeSubJourney == ControlCodeSubJourney.PHYSICAL_GOODS_SEARCH) {
        this.formAction = routes.NotApplicableController.handleSubmit().url();
      }
      else {
        GoodsType goodsType = controlCodeSubJourney.getSoftTechGoodsType();
        this.formAction = routes.NotApplicableController.handleSearchRelatedToSubmit(goodsType.urlString()).url();
      }
    }
    else if (controlCodeSubJourney.isSoftTechControlsVariant()) {
      this.formAction = routes.NotApplicableController.handleSubmit().url();
    }
    else if (controlCodeSubJourney == ControlCodeSubJourney.SOFTWARE_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      this.formAction = routes.AdditionalSpecificationsController.handleRelatedControlsSubmit(GoodsType.SOFTWARE.urlString()).url();
    }
    else if (controlCodeSubJourney == ControlCodeSubJourney.TECHNOLOGY_CONTROLS_RELATED_TO_A_PHYSICAL_GOOD) {
      this.formAction = routes.AdditionalSpecificationsController.handleRelatedControlsSubmit(GoodsType.TECHNOLOGY.urlString()).url();
    }
    else if (controlCodeSubJourney == ControlCodeSubJourney.SOFTWARE_CATCHALL_CONTROLS) {
      this.formAction = routes.AdditionalSpecificationsController.handleCatchallControlsSubmit(GoodsType.SOFTWARE.urlString()).url();
    }
    else if (controlCodeSubJourney == ControlCodeSubJourney.TECHNOLOGY_CATCHALL_CONTROLS) {
      this.formAction = routes.AdditionalSpecificationsController.handleCatchallControlsSubmit(GoodsType.TECHNOLOGY.urlString()).url();
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
  }

}
