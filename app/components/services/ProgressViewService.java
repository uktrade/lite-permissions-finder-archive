package components.services;

import models.view.ProgressView;
import triage.config.StageConfig;

public interface ProgressViewService {
  ProgressView createProgressView(StageConfig stageConfig);
}
