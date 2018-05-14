package components.services;

import models.view.AnswerView;
import triage.config.StageConfig;

import java.util.List;

public interface AnswerViewService {
  List<AnswerView> createAnswerViews(StageConfig stageConfig);
}
