package components.services;

import triage.config.AnswerConfig;
import triage.config.StageConfig;

import java.util.List;

public interface AnswerConfigService {
  List<AnswerConfig> getMatchingAnswerConfigs(List<String> actualAnswers, StageConfig stageConfig);

  AnswerConfig getAnswerConfigWithLowestPrecedence(List<AnswerConfig> answerConfigs);

  List<AnswerConfig> getControlEntryFoundOutcomeAnswerConfigs(List<AnswerConfig> answerConfigs);

  boolean isControlEntryFoundOutcomeAnswerConfig(AnswerConfig answerConfig);
}
