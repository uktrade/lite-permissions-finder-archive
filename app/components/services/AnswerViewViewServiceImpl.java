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
      return createAnswerViewFromControlEntryConfig(answerConfig, associatedControlEntryConfig.get());
    } else {
      Optional<RichText> labelText = answerConfig.getLabelText();
      if (labelText.isPresent()) {
        return createAnswerViewFromLabelText(answerConfig, labelText.get());
      } else {
        throw new BusinessRuleException("Both answerConfig.getAssociatedControlEntryConfig and answerConfig.getLabelText are absent.");
      }
    }
  }

  private AnswerView createAnswerViewFromControlEntryConfig(AnswerConfig answerConfig,
                                                            ControlEntryConfig controlEntryConfig) {
    String prompt = htmlRenderService.convertRichTextToPlainText(controlEntryConfig.getFullDescription());
    List<SubAnswerView> subAnswerViews;
    if (answerConfig.getNestedContent().isPresent()) {
      subAnswerViews = new ArrayList<>();
    } else {
      subAnswerViews = createSubAnswerViews(controlEntryConfig);
    }
    String moreInformation;
    if (!subAnswerViews.isEmpty()) {
      moreInformation = "";
    } else {
      Optional<String> nextStageId = answerConfig.getNextStageId();
      if (nextStageId.isPresent()) {
        moreInformation = createMoreInformation(nextStageId.get());
      } else {
        moreInformation = "";
      }
    }
    String nestedContent = createdNestedContent(answerConfig);
    return new AnswerView(prompt, answerConfig.getAnswerId(), answerConfig.isDividerAbove(), subAnswerViews,
        nestedContent, moreInformation);
  }

  private AnswerView createAnswerViewFromLabelText(AnswerConfig answerConfig, RichText labelText) {
    String prompt = htmlRenderService.convertRichTextToPlainText(labelText);
    String nestedContent = createdNestedContent(answerConfig);
    String moreInformation;
    Optional<String> nextStageId = answerConfig.getNextStageId();
    if (nextStageId.isPresent()) {
      moreInformation = createMoreInformation(nextStageId.get());
    } else {
      moreInformation = "";
    }
    return new AnswerView(prompt, answerConfig.getAnswerId(), answerConfig.isDividerAbove(), new ArrayList<>(),
        nestedContent, moreInformation);
  }

  @Override
  public List<SubAnswerView> createSubAnswerViews(ControlEntryConfig controlEntryConfig) {
    if (controlEntryConfig.hasNestedChildren()) {
      return journeyConfigService.getChildRatings(controlEntryConfig).stream()
          .map(this::createSubAnswerView)
          .collect(Collectors.toList());
    } else {
      return new ArrayList<>();
    }
  }

  private String createMoreInformation(String stageId) {
    StageConfig stageConfig = journeyConfigService.getStageConfigById(stageId);
    if (stageConfig.getQuestionType() == StageConfig.QuestionType.STANDARD) {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("<ul>");
      for (AnswerConfig answerConfig : stageConfig.getAnswerConfigs()) {
        stringBuilder.append("<li>");
        Optional<ControlEntryConfig> associatedControlEntryConfig = answerConfig.getAssociatedControlEntryConfig();
        if (associatedControlEntryConfig.isPresent()) {
          ControlEntryConfig controlEntryConfig = associatedControlEntryConfig.get();
          stringBuilder.append(htmlRenderService.convertRichTextToPlainText(controlEntryConfig.getFullDescription()));
          String subAnswerViewsToHtml = subAnswerViewsToHtml(createSubAnswerViews(controlEntryConfig));
          stringBuilder.append(subAnswerViewsToHtml);
        } else if (answerConfig.getLabelText().isPresent()) {
          stringBuilder.append(htmlRenderService.convertRichTextToHtml(answerConfig.getLabelText().get()));
        } else {
          throw new BusinessRuleException("Both answerConfig.getAssociatedControlEntryConfig and answerConfig.getLabelText are absent.");
        }
        stringBuilder.append("</li>");
      }
      stringBuilder.append("</ul>");
      return stringBuilder.toString();
    } else {
      Optional<String> nextStageId = stageConfig.getNextStageId();
      if (nextStageId.isPresent()) {
        return createMoreInformation(nextStageId.get());
      } else {
        return "";
      }
    }
  }

  private String subAnswerViewsToHtml(List<SubAnswerView> subAnswerViews) {
    StringBuilder stringBuilder = new StringBuilder();
    if (!subAnswerViews.isEmpty()) {
      stringBuilder.append("<ul>");
      for (SubAnswerView subAnswerView : subAnswerViews) {
        stringBuilder.append("<li>");
        stringBuilder.append(subAnswerView.getText());
        stringBuilder.append("</li>");
        stringBuilder.append(subAnswerViewsToHtml(subAnswerView.getSubAnswerViews()));
      }
      stringBuilder.append("</ul>");
    }
    return stringBuilder.toString();
  }

  private String createdNestedContent(AnswerConfig answerConfig) {
    Optional<RichText> nestedContent = answerConfig.getNestedContent();
    if (nestedContent.isPresent()) {
      return htmlRenderService.convertRichTextToHtmlWithoutLinks(nestedContent.get());
    } else {
      return "";
    }
  }

  private SubAnswerView createSubAnswerView(ControlEntryConfig controlEntryConfig) {
    String summaryDescription = htmlRenderService.convertRichTextToPlainText(controlEntryConfig.getFullDescription());
    List<SubAnswerView> subAnswerViews = createSubAnswerViews(controlEntryConfig);
    return new SubAnswerView(summaryDescription, subAnswerViews);
  }

}
