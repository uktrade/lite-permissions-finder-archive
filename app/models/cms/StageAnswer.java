package models.cms;

import models.cms.enums.OutcomeType;

public class StageAnswer {
  private Long id;
  private Long parentStageId;
  private Long goToStageId;
  private OutcomeType goToOutcomeType;
  private Long controlEntryId;
  private String answerText;
  private Integer displayOrder;
  private Integer answerPrecedence;
  private Boolean dividerAbove;
  private String nestedContent;
  private String moreInfoContent;

  public Long getId() {
    return id;
  }

  public StageAnswer setId(Long id) {
    this.id = id;
    return this;
  }

  public Long getParentStageId() {
    return parentStageId;
  }

  public StageAnswer setParentStageId(Long parentStageId) {
    this.parentStageId = parentStageId;
    return this;
  }

  public Long getGoToStageId() {
    return goToStageId;
  }

  public StageAnswer setGoToStageId(Long goToStageId) {
    this.goToStageId = goToStageId;
    return this;
  }

  public OutcomeType getGoToOutcomeType() {
    return goToOutcomeType;
  }

  public StageAnswer setGoToOutcomeType(OutcomeType goToOutcomeType) {
    this.goToOutcomeType = goToOutcomeType;
    return this;
  }

  public Long getControlEntryId() {
    return controlEntryId;
  }

  public StageAnswer setControlEntryId(Long controlEntryId) {
    this.controlEntryId = controlEntryId;
    return this;
  }

  public String getAnswerText() {
    return answerText;
  }

  public StageAnswer setAnswerText(String answerText) {
    this.answerText = answerText;
    return this;
  }

  public Integer getDisplayOrder() {
    return displayOrder;
  }

  public StageAnswer setDisplayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder;
    return this;
  }

  public Integer getAnswerPrecedence() {
    return answerPrecedence;
  }

  public StageAnswer setAnswerPrecedence(Integer answerPrecedence) {
    this.answerPrecedence = answerPrecedence;
    return this;
  }

  public Boolean isDividerAbove() {
    return dividerAbove;
  }

  public StageAnswer setDividerAbove(Boolean dividerAbove) {
    this.dividerAbove = dividerAbove;
    return this;
  }

  public String getNestedContent() {
    return nestedContent;
  }

  public StageAnswer setNestedContent(String nestedContent) {
    this.nestedContent = nestedContent;
    return this;
  }

  public String getMoreInfoContent() {
    return moreInfoContent;
  }

  public StageAnswer setMoreInfoContent(String moreInfoContent) {
    this.moreInfoContent = moreInfoContent;
    return this;
  }
}
