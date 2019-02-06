package triage.config;

import models.cms.enums.AnswerType;
import models.cms.enums.OutcomeType;
import models.cms.enums.QuestionType;
import triage.text.RichText;

import java.util.List;
import java.util.Optional;

public class StageConfig {

  private final String stageId;
  private final Long journeyId;
  private final String questionTitle;
  private final RichText explanatoryNote;

  private final QuestionType questionType;
  private final AnswerType answerType;

  private final String nextStageId;
  private final OutcomeType outcomeType;

  private final boolean decontrolled;

  private final ControlEntryConfig relatedControlEntry;

  private final List<AnswerConfig> answerConfigs;

  public StageConfig(String stageId, String questionTitle, RichText explanatoryNote,
                     QuestionType questionType, AnswerType answerType, String nextStageId,
                     OutcomeType outcomeType, boolean decontrolled, ControlEntryConfig relatedControlEntry,
                     List<AnswerConfig> answerConfigs, Long journeyId) {
    this.stageId = stageId;
    this.questionTitle = questionTitle;
    this.explanatoryNote = explanatoryNote;
    this.questionType = questionType;
    this.answerType = answerType;
    this.nextStageId = nextStageId;
    this.outcomeType = outcomeType;
    this.decontrolled = decontrolled;
    this.relatedControlEntry = relatedControlEntry;
    this.answerConfigs = answerConfigs;
    this.journeyId = journeyId;
  }

  @Override
  public String toString() {
    return "StageConfig{" +
            "stageId='" + stageId + '\'' +
            "journeyId='" + journeyId + '\'' +
            ", questionTitle='" + questionTitle + '\'' +
            ", explanatoryNote=" + explanatoryNote +
            ", questionType=" + questionType +
            ", answerType=" + answerType +
            ", nextStageId='" + nextStageId + '\'' +
            ", outcomeType=" + outcomeType +
            ", decontrolled=" + decontrolled +
            ", relatedControlEntry=" + relatedControlEntry +
            ", answerConfigs=" + answerConfigs +
            '}';
  }

  public boolean isDecontrolled() {
    return decontrolled;
  }

  public String getStageId() {
    return stageId;
  }

  public Long getJourneyId() {
    return journeyId;
  }

  public Optional<String> getQuestionTitle() {
    return Optional.ofNullable(questionTitle);
  }

  public Optional<RichText> getExplanatoryNote() {
    return Optional.ofNullable(explanatoryNote);
  }

  public QuestionType getQuestionType() {
    return questionType;
  }

  public AnswerType getAnswerType() {
    return answerType;
  }

  //non-empty if if questionType == DECONTROL
  public Optional<String> getNextStageId() {
    return Optional.ofNullable(nextStageId);
  }

  public Optional<OutcomeType> getOutcomeType() {
    return Optional.ofNullable(outcomeType);
  }

  public Optional<ControlEntryConfig> getRelatedControlEntry() {
    return Optional.ofNullable(relatedControlEntry);
  }

  public List<AnswerConfig> getAnswerConfigs() {
    return answerConfigs;
  }
}
