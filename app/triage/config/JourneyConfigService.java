package triage.config;

import models.cms.StageAnswer;
import models.cms.enums.StageAnswerOutcomeType;

import java.util.List;

public interface JourneyConfigService {

  String getInitialStageId();

  StageConfig getStageConfigById(String stageId);

  List<NoteConfig> getNoteConfigsByStageId(String stageId);

  ControlEntryConfig getControlEntryConfigById(String controlEntryId);

  //List of an entry's immediate children
  List<ControlEntryConfig> getChildRatings(ControlEntryConfig controlEntryConfig);

  List<String> getStageIdsForControlEntry(ControlEntryConfig controlEntryConfig);

  List<StageAnswer> getStageAnswersByControlEntryIdAndOutcomeType(long controlEntryId,
                                                                  StageAnswerOutcomeType stageAnswerOutcomeType);

  AnswerConfig getStageAnswerForPreviousStage(String stageId);

  StageConfig getStageConfigForPreviousStage(String stageId);

}
