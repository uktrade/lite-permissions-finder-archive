package models.controlcode;

import controllers.controlcode.routes;
import models.GoodsType;
import models.softtech.ApplicableSoftTechControls;
import play.data.Form;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotApplicableDisplay {
  public final Form<?> form;
  public final String formAction;
  public final String controlCodeAlias;
  public final boolean showExtendedContent;
  public final ControlCodeSubJourney controlCodeSubJourney;
  public final List<NotApplicableButton> buttons;

  public class NotApplicableButton implements Comparable<NotApplicableButton> {
    public final String value;
    public final String content;
    public final int order;

    public NotApplicableButton(int order, String value, String content) {
      this.order = order;
      this.value = value;
      this.content = content;
    }

    @Override
    public int compareTo(NotApplicableButton otherButton) {
      return this.order - otherButton.order;
    }
  }

  /**
   * Display object for the {@code notApplicable} view
   * @param controlCodeSubJourney The control code journey
   * @param form The form
   * @param controlCodeAlias The control code alias
   * @param showExtendedContent Show the extended content
   * @param applicableSoftTechControls Should be {@code null} unless {@code controlCodeSubJourney == }{@link ControlCodeSubJourney#SOFTWARE_CONTROLS}.
   */
  public NotApplicableDisplay(ControlCodeSubJourney controlCodeSubJourney, Form<?> form, String controlCodeAlias, boolean showExtendedContent, ApplicableSoftTechControls applicableSoftTechControls) {
    this.controlCodeSubJourney = controlCodeSubJourney;
    this.form = form;
    this.controlCodeAlias = controlCodeAlias;
    this.showExtendedContent = showExtendedContent;
    if (controlCodeSubJourney.isPhysicalGoodsSearchVariant()) {
      this.formAction = routes.NotApplicableController.handleSubmit().url();
      this.buttons = Arrays.asList(
          new NotApplicableButton(1, "backToResults", "return to the list of possible matches and choose again"),
          new NotApplicableButton(2, "backToSearch", "edit your item description to get a different set of results")
      );
    }
    else if (controlCodeSubJourney.isSoftTechControlsVariant() ||
        controlCodeSubJourney.isSoftTechControlsRelatedToPhysicalGoodVariant()) {
      this.formAction = routes.NotApplicableController.handleSubmit().url();
      if (canPickAgain(applicableSoftTechControls)) {
        this.buttons = Collections.singletonList(new NotApplicableButton(1, "backToMatches", "return to the list of possible matches and choose again"));
      }
      else {
        this.buttons = Collections.singletonList(new NotApplicableButton(1, "continue", "continue to other options"));
      }
    }
    else if (controlCodeSubJourney.isSoftTechCatchallControlsVariant()) {
      GoodsType goodsType = controlCodeSubJourney.getSoftTechGoodsType();
      this.formAction = routes.NotApplicableController.handleCatchallControlsSubmit(goodsType.urlString()).url();
      if (canPickAgain(applicableSoftTechControls)) {
        this.buttons = Collections.singletonList(new NotApplicableButton(1, "backToMatches", "return to the list of possible matches and choose again"));
      }
      else {
        this.buttons = Collections.singletonList(new NotApplicableButton(1, "continue", "continue to other options"));
      }
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ControlCodeSubJourney enum: \"%s\""
          , controlCodeSubJourney.toString()));
    }
    this.buttons.sort(Comparator.naturalOrder());
  }

  private boolean canPickAgain(ApplicableSoftTechControls applicableSoftTechControls) {
    if (applicableSoftTechControls == ApplicableSoftTechControls.ONE) {
      return false;
    }
    else if (applicableSoftTechControls == ApplicableSoftTechControls.GREATER_THAN_ONE) {
      return true;
    }
    else {
      throw new RuntimeException(String.format("Unexpected member of ApplicableSoftTechControls enum: \"%s\""
          , applicableSoftTechControls.toString()));
    }
  }

  /**
   * Display object for the {@code notApplicable} view
   * @param controlCodeSubJourney The control code journey
   * @param form The form
   * @param controlCodeAlias The control code alias
   * @param showExtendedContent Show the extended content
   */
  public NotApplicableDisplay(ControlCodeSubJourney controlCodeSubJourney, Form<?> form, String controlCodeAlias, boolean showExtendedContent) {
    this(controlCodeSubJourney, form, controlCodeAlias, showExtendedContent, null);
  }
}
