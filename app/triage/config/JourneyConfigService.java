package triage.config;

import models.cms.enums.OutcomeType;

import java.util.List;
import java.util.Optional;

public interface JourneyConfigService {

  long getDefaultJourneyId();

  String getInitialStageId();

  StageConfig getStageConfigById(String stageId);

  List<NoteConfig> getNoteConfigsByStageId(String stageId);

  ControlEntryConfig getControlEntryConfigById(String controlEntryId);

  //List of an entry's immediate children
  List<ControlEntryConfig> getChildRatings(ControlEntryConfig controlEntryConfig);

  List<String> getStageIdsForControlEntry(ControlEntryConfig controlEntryConfig);

  List<StageConfig> getStageConfigsByControlEntryIdAndOutcomeType(String controlEntryId, OutcomeType outcomeType);

  AnswerConfig getStageAnswerForPreviousStage(String stageId);

  StageConfig getStageConfigForPreviousStage(String stageId);

  Optional<StageConfig> getPrincipleStageConfigForControlEntry(ControlEntryConfig controlEntryConfig);

  List<ControlEntryConfig> getRelatedControlEntries(ControlEntryConfig controlEntryConfig);

  void flushCache();

}
