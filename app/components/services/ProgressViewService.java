package components.services;

import models.view.ProgressView;
import triage.config.ControlEntryConfig;
import triage.config.StageConfig;

public interface ProgressViewService {
  ProgressView createProgressView(ControlEntryConfig controlEntryConfig);

  ProgressView createProgressView(StageConfig stageConfig);
}
