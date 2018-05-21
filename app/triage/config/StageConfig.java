package triage.config;

import triage.text.RichText;

import java.util.List;
import java.util.Optional;

public class StageConfig {

  public enum QuestionType {
    STANDARD, DECONTROL;
  }

  public enum AnswerType {
    SELECT_ONE, SELECT_MANY;
  }

  private final String stageId;
  private final String questionTitle;
  private final RichText explanatoryNote;

  private final QuestionType questionType;
  private final AnswerType answerType;

  private final String nextStageId;
  private final OutcomeType outcomeType;

  private final ControlEntryConfig relatedControlEntry;

  private final List<AnswerConfig> answerConfigs;

  public StageConfig(String stageId, String questionTitle, RichText explanatoryNote,
                     QuestionType questionType, AnswerType answerType, String nextStageId,
                     OutcomeType outcomeType, ControlEntryConfig relatedControlEntry,
                     List<AnswerConfig> answerConfigs) {
    this.stageId = stageId;
    this.questionTitle = questionTitle;
    this.explanatoryNote = explanatoryNote;
    this.questionType = questionType;
    this.answerType = answerType;
    this.nextStageId = nextStageId;
    this.outcomeType = outcomeType;
    this.relatedControlEntry = relatedControlEntry;
    this.answerConfigs = answerConfigs;
  }

  public String getStageId() {
    return stageId;
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
