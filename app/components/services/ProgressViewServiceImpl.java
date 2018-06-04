package components.services;

import com.google.inject.Inject;
import models.view.ProgressView;
import triage.config.AnswerConfig;
import triage.config.ControlEntryConfig;
import triage.config.JourneyConfigService;
import triage.config.StageConfig;
import triage.text.HtmlRenderService;

public class ProgressViewServiceImpl implements ProgressViewService {

  private final BreadcrumbViewService breadcrumbViewService;
  private final RenderService renderService;
  private final HtmlRenderService htmlRenderService;
  private final JourneyConfigService journeyConfigService;

  @Inject
  public ProgressViewServiceImpl(BreadcrumbViewService breadcrumbViewService,
                                 RenderService renderService, HtmlRenderService htmlRenderService,
                                 JourneyConfigService journeyConfigService) {
    this.breadcrumbViewService = breadcrumbViewService;
    this.renderService = renderService;
    this.htmlRenderService = htmlRenderService;
    this.journeyConfigService = journeyConfigService;
  }

  @Override
  public ProgressView createProgressView(StageConfig stageConfig) {
    ControlEntryConfig controlEntryConfig = breadcrumbViewService.getControlEntryConfig(stageConfig);
    String code;
    String description;
    if (controlEntryConfig != null) {
      code = controlEntryConfig.getControlCode();
      description = renderService.getSummaryDescription(controlEntryConfig);
    } else {
      AnswerConfig answerConfig = journeyConfigService.getStageAnswerForPreviousStage(stageConfig.getStageId());
      if (answerConfig != null && answerConfig.getLabelText().isPresent()) {
        code = null;
        description = htmlRenderService.convertRichText(answerConfig.getLabelText().get(), false);
      } else {
        code = null;
        description = "UK Military List";
      }
    }
    return new ProgressView(code, description);
  }

}
