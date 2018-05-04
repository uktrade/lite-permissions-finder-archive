package triage.config;

import java.util.List;

public interface JourneyConfigService {

  String getInitialStageId();

  StageConfig getStageConfigForStageId(String stageId);

  List<NoteConfig> getNotesForStageId(String stageId);

  //Hierarchy of parents
  List<ControlEntryConfig> getParentRatings(ControlEntryConfig controlEntryConfig);

  //Hierarchy of children
  List<ControlEntryConfig> getChildRatings(ControlEntryConfig controlEntryConfig);

}
