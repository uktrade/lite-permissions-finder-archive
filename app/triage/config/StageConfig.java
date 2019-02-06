package triage.config;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import models.cms.enums.AnswerType;
import models.cms.enums.OutcomeType;
import models.cms.enums.QuestionType;
import triage.text.RichText;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor(onConstructor = @__({ @Inject}))
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
