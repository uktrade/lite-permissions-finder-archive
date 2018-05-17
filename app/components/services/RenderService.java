package components.services;

import triage.config.ControlEntryConfig;
import triage.config.StageConfig;

public interface RenderService {
  String getExplanatoryText(StageConfig stageConfig);

  String getFullDescription(ControlEntryConfig controlEntryConfig);

  String getSummaryDescription(ControlEntryConfig controlEntryConfig);
}
