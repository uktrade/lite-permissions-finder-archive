package triage.config;

import triage.text.RichText;

import java.util.Optional;

public class AnswerConfig {

  private final String answerId;

  private final String nextStageId;
  private final OutcomeType outcomeType;

  private final RichText labelText;
  private final RichText nestedContent;
  private final RichText moreInfoContent;

  private final ControlEntryConfig associatedControlEntryConfig;

  private final int displayOrder;
  private final int answerPrecedence;

  private final boolean dividerAbove;

  public AnswerConfig(String answerId, String nextStageId, OutcomeType outcomeType,
                      ControlEntryConfig associatedControlEntryConfig, int displayOrder, boolean dividerAbove) {
    this.answerId = answerId;
    this.nextStageId = nextStageId;
    this.outcomeType = outcomeType;
    this.labelText = null;
    this.nestedContent = null;
    this.moreInfoContent = null;
    this.associatedControlEntryConfig = associatedControlEntryConfig;
    this.displayOrder = displayOrder;
    this.answerPrecedence = displayOrder;
    this.dividerAbove = dividerAbove;
  }


  public AnswerConfig(String answerId, String nextStageId, OutcomeType outcomeType, RichText labelText,
                      RichText nestedContent, RichText moreInfoContent,
                      ControlEntryConfig associatedControlEntryConfig, int displayOrder, int answerPrecedence,
                      boolean dividerAbove) {
    this.answerId = answerId;
    this.nextStageId = nextStageId;
    this.outcomeType = outcomeType;
    this.labelText = labelText;
    this.nestedContent = nestedContent;
    this.moreInfoContent = moreInfoContent;
    this.associatedControlEntryConfig = associatedControlEntryConfig;
    this.displayOrder = displayOrder;
    this.answerPrecedence = answerPrecedence;
    this.dividerAbove = dividerAbove;
  }

  public String getAnswerId() {
    return answerId;
  }

  //Mutually exclusive with getOutcomeType
  public Optional<String> getNextStageId() {
    return Optional.ofNullable(nextStageId);
  }

  //Mutually exclusive with getNextStageId
  public Optional<OutcomeType> getOutcomeType() {
    return Optional.ofNullable(outcomeType);
  }

  //If empty, use ControlEntryConfig fullDescription
  public Optional<RichText> getLabelText() {
    return Optional.ofNullable(labelText);
  }

  public Optional<RichText> getNestedContent() {
    return Optional.ofNullable(nestedContent);
  }

  public Optional<RichText> getMoreInfoContent() {
    return Optional.ofNullable(moreInfoContent);
  }

  public Optional<ControlEntryConfig> getAssociatedControlEntryConfig() {
    return Optional.ofNullable(associatedControlEntryConfig);
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public int getAnswerPrecedence() {
    return answerPrecedence;
  }

  public boolean isDividerAbove() {
    return dividerAbove;
  }
}
