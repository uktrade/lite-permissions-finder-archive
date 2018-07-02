package triage.cache;

import triage.config.ControlEntryConfig;
import triage.config.NoteConfig;
import triage.config.StageConfig;

import java.util.List;
import java.util.Optional;

public interface JourneyConfigFactory {
  Optional<StageConfig> createStageConfigForId(String stageId);

  Optional<ControlEntryConfig> createControlEntryConfigForId(String controlEntryId);

  List<ControlEntryConfig> createRelatedControlEntryConfigsForId(String controlEntryId);

  List<NoteConfig> createNoteConfigsForStageId(String stageId);
}
