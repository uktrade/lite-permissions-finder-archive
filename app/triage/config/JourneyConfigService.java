package triage.config;

import java.util.List;

public interface JourneyConfigService {

  String getInitialStageId();

  StageConfig getStageConfigById(String stageId);

  List<NoteConfig> getNoteConfigsByStageId(String stageId);

  ControlEntryConfig getControlEntryConfigById(String controlEntryId);

  //List of an entry's immediate children
  List<ControlEntryConfig> getChildRatings(ControlEntryConfig controlEntryConfig);

  List<String> getStageIdsForControlEntry(ControlEntryConfig controlEntryConfig);

}
