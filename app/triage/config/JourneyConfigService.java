package triage.config;

import java.util.List;
import java.util.Optional;

public interface JourneyConfigService {

  Optional<StageConfig> getStageConfigById(String stageId);

  String getJourneyNameByJourneyId(long journeyId);

  StageConfig getStageConfigForInitialJourneyStage(long journeyId);

  List<String> getInitialStagesForAllJourneys();

  List<NoteConfig> getNoteConfigsByStageId(String stageId);

  Optional<ControlEntryConfig> getControlEntryConfigById(String controlEntryId);

  //List of an entry's immediate children
  List<ControlEntryConfig> getChildRatings(ControlEntryConfig controlEntryConfig);

  List<String> getStageIdsForControlEntryId(String controlEntryId);

  AnswerConfig getStageAnswerForPreviousStage(String stageId);

  StageConfig getStageConfigForPreviousStage(String stageId);

  Optional<StageConfig> getPrincipleStageConfigForControlEntry(ControlEntryConfig controlEntryConfig);

  List<ControlEntryConfig> getRelatedControlEntries(ControlEntryConfig controlEntryConfig);

  void flushCache();
}
