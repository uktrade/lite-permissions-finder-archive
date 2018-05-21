package components.services;

import triage.config.AnswerConfig;
import triage.config.OutcomeType;
import triage.config.StageConfig;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AnswerConfigServiceImpl implements AnswerConfigService {

  @Override
  public List<AnswerConfig> getMatchingAnswerConfigs(List<String> actualAnswers, StageConfig stageConfig) {
    return stageConfig.getAnswerConfigs()
        .stream()
        .filter(answerConfig -> actualAnswers.contains(answerConfig.getAnswerId()))
        .collect(Collectors.toList());
  }

  @Override
  public AnswerConfig getAnswerConfigWithLowestPrecedence(List<AnswerConfig> answerConfigs) {
    return answerConfigs.stream()
        .sorted(Comparator.comparing(AnswerConfig::getAnswerPrecedence))
        .findFirst()
        .orElse(null);
  }

  @Override
  public List<AnswerConfig> getControlEntryFoundOutcomeAnswerConfigs(List<AnswerConfig> answerConfigs) {
    return answerConfigs.stream()
        .filter(this::isControlEntryFoundOutcomeAnswerConfig)
        .collect(Collectors.toList());
  }

  @Override
  public boolean isControlEntryFoundOutcomeAnswerConfig(AnswerConfig answerConfig) {
    return answerConfig.getOutcomeType().isPresent() &&
        answerConfig.getOutcomeType().get() == OutcomeType.CONTROL_ENTRY_FOUND;
  }

}
