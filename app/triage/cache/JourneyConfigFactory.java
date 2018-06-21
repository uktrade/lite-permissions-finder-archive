package triage.cache;

import triage.config.ControlEntryConfig;
import triage.config.NoteConfig;
import triage.config.StageConfig;

import java.util.List;

public interface JourneyConfigFactory {
  StageConfig createStageConfigForId(String stageId);

  ControlEntryConfig createControlEntryConfigForId(String controlEntryId);

  List<ControlEntryConfig> createRelatedControlEntryConfigsForId(String controlEntryId);

  List<NoteConfig> createNoteConfigsForStageId(String stageId);
}
