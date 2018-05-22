package components.services;

import com.google.inject.Inject;
import exceptions.BusinessRuleException;
import models.view.AnswerView;
import models.view.SubAnswerView;
import triage.config.AnswerConfig;
import triage.config.ControlEntryConfig;
import triage.config.JourneyConfigService;
import triage.config.StageConfig;
import triage.text.HtmlRenderService;
import triage.text.RichText;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnswerViewViewServiceImpl implements AnswerViewService {

  private final HtmlRenderService htmlRenderService;
  private final JourneyConfigService journeyConfigService;

  @Inject
  public AnswerViewViewServiceImpl(HtmlRenderService htmlRenderService,
                                   JourneyConfigService journeyConfigService) {
    this.htmlRenderService = htmlRenderService;
    this.journeyConfigService = journeyConfigService;
  }

  @Override
  public List<AnswerView> createAnswerViews(StageConfig stageConfig) {
    return stageConfig.getAnswerConfigs().stream()
        .sorted(Comparator.comparing(AnswerConfig::getDisplayOrder))
        .map(this::createAnswerView)
        .collect(Collectors.toList());
  }

  private AnswerView createAnswerView(AnswerConfig answerConfig) {
    Optional<ControlEntryConfig> associatedControlEntryConfig = answerConfig.getAssociatedControlEntryConfig();
    if (associatedControlEntryConfig.isPresent()) {
      // consider nested children
      ControlEntryConfig controlEntryConfig = associatedControlEntryConfig.get();
      List<SubAnswerView> subAnswerViews = createSubAnswerViews(controlEntryConfig);
      return new AnswerView(htmlRenderService.convertRichTextToPlainText(controlEntryConfig.getFullDescription()),
          answerConfig.getAnswerId(),
          subAnswerViews);
    } else {
      Optional<RichText> labelText = answerConfig.getLabelText();
      if (labelText.isPresent()) {
        return new AnswerView(htmlRenderService.convertRichTextToPlainText(labelText.get()), answerConfig.getAnswerId(), new ArrayList<>());
      } else {
        throw new BusinessRuleException("Both answerConfig.getAssociatedControlEntryConfig and answerConfig.getLabelText are absent.");
      }
    }
  }

  private List<SubAnswerView> createSubAnswerViews(ControlEntryConfig controlEntryConfig) {
    if (controlEntryConfig.hasNestedChildren()) {
      return journeyConfigService.getChildRatings(controlEntryConfig).stream()
          .map(this::createSubAnswerView)
          .collect(Collectors.toList());
    } else {
      return new ArrayList<>();
    }
  }

  private SubAnswerView createSubAnswerView(ControlEntryConfig controlEntryConfig) {
    String summaryDescription = htmlRenderService.convertRichTextToPlainText(controlEntryConfig.getFullDescription());
    List<SubAnswerView> subAnswerViews = createSubAnswerViews(controlEntryConfig);
    return new SubAnswerView(summaryDescription, subAnswerViews);
  }

}
