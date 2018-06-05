package components.services;

import models.view.AnswerView;
import models.view.SubAnswerView;
import triage.config.ControlEntryConfig;
import triage.config.StageConfig;

import java.util.List;

public interface AnswerViewService {
  List<AnswerView> createAnswerViews(StageConfig stageConfig, boolean html);

  List<SubAnswerView> createSubAnswerViews(ControlEntryConfig controlEntryConfig);
}
