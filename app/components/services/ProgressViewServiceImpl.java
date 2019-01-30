package components.services;

import com.google.inject.Inject;
import components.cms.dao.JourneyDao;
import components.cms.dao.StageDao;
import jodd.typeconverter.Convert;
import lombok.AllArgsConstructor;
import models.cms.Journey;
import models.cms.Stage;
import models.view.ProgressView;
import triage.config.AnswerConfig;
import triage.config.ControlEntryConfig;
import triage.config.JourneyConfigService;
import triage.config.StageConfig;
import triage.text.HtmlRenderService;
import triage.text.RichText;
import utils.ListNameToFriendlyNameUtil;

import java.util.Optional;

@AllArgsConstructor(onConstructor = @__({ @Inject }))
public class ProgressViewServiceImpl implements ProgressViewService {

  private final BreadcrumbViewService breadcrumbViewService;
  private final RenderService renderService;
  private final HtmlRenderService htmlRenderService;
  private final JourneyConfigService journeyConfigService;
  private final StageDao stageDao;
  private final JourneyDao journeyDao;

  @Override
  public ProgressView createProgressView(ControlEntryConfig controlEntryConfig) {
    String code = controlEntryConfig.getControlCode();
    String description = renderService.getSummaryDescription(controlEntryConfig);
    return new ProgressView(code, description);
  }

  @Override
  public ProgressView createProgressView(StageConfig stageConfig) {
    ControlEntryConfig controlEntryConfig = breadcrumbViewService.getControlEntryConfig(stageConfig);
    String code;
    String description;

    Stage stage = stageDao.getStage(Convert.toInteger(stageConfig.getStageId()));
    Journey journey = journeyDao.getJourney(stage.getJourneyId());

    if (controlEntryConfig != null) {
      code = controlEntryConfig.getControlCode();
      description = renderService.getSummaryDescription(controlEntryConfig);
    } else {
      AnswerConfig answerConfig = journeyConfigService.getStageAnswerForPreviousStage(stageConfig.getStageId());
      if (answerConfig != null) {
        Optional<RichText> labelTextOptional = answerConfig.getLabelText();
        if (labelTextOptional.isPresent()) {
          code = null;
          description = htmlRenderService.convertRichTextToPlainText(labelTextOptional.get());
        } else {
          code = null;
          description = ListNameToFriendlyNameUtil.GetFriendlyNameFromListName(journey.getJourneyName());
        }
      } else {
        code = null;
        description = ListNameToFriendlyNameUtil.GetFriendlyNameFromListName(journey.getJourneyName());
      }
    }
    return new ProgressView(code, description);
  }

}
