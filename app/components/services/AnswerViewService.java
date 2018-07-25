package components.services;

import models.view.AnswerView;
import models.view.SubAnswerView;
import triage.config.ControlEntryConfig;
import triage.config.StageConfig;
import triage.text.HtmlRenderOption;

import java.util.List;

public interface AnswerViewService {
  List<AnswerView> createAnswerViews(StageConfig stageConfig, boolean html);

  List<AnswerView> createAnswerViewsFromControlEntryConfigs(List<ControlEntryConfig> controlEntryConfigs);

  List<SubAnswerView> createSubAnswerViews(ControlEntryConfig controlEntryConfig, boolean html,
                                           HtmlRenderOption... htmlRenderOptions);

  String createSubAnswerViewsHtml(List<SubAnswerView> subAnswerViews);
}
