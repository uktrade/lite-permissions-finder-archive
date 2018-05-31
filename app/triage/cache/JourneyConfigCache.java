package triage.cache;

import triage.config.ControlEntryConfig;
import triage.config.NoteConfig;
import triage.config.StageConfig;

import java.util.List;

public interface JourneyConfigCache {

  StageConfig getStageConfigById(String stageId);

  List<NoteConfig> getNoteConfigsByStageId(String stageId);

  ControlEntryConfig getControlEntryConfigById(String controlEntryId);

  void flushCache();

}
