package components.services;

import com.google.inject.Inject;
import exceptions.BusinessRuleException;
import models.view.AnswerView;
import models.view.SubAnswerView;
import org.apache.commons.lang3.StringUtils;
import triage.config.AnswerConfig;
import triage.config.ControlEntryConfig;
import triage.config.JourneyConfigService;
import triage.config.StageConfig;
import triage.text.HtmlRenderOption;
import triage.text.HtmlRenderService;
import triage.text.RichText;
import triage.text.SubAnswer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnswerViewServiceImpl implements AnswerViewService {

  private final HtmlRenderService htmlRenderService;
  private final JourneyConfigService journeyConfigService;

  @Inject
  public AnswerViewServiceImpl(HtmlRenderService htmlRenderService,
                               JourneyConfigService journeyConfigService) {
    this.htmlRenderService = htmlRenderService;
    this.journeyConfigService = journeyConfigService;
  }

  @Override
  public List<AnswerView> createAnswerViews(StageConfig stageConfig, boolean html) {
    return stageConfig.getAnswerConfigs().stream()
        .sorted(Comparator.comparing(AnswerConfig::getDisplayOrder))
        .map(answerConfig -> createAnswerView(answerConfig, html))
        .collect(Collectors.toList());
  }

  private AnswerView createAnswerView(AnswerConfig answerConfig, boolean html) {
    Optional<ControlEntryConfig> associatedControlEntryConfig = answerConfig.getAssociatedControlEntryConfig();
    if (associatedControlEntryConfig.isPresent()) {
      return createAnswerViewFromControlEntryConfig(answerConfig, associatedControlEntryConfig.get(), html);
    } else {
      Optional<RichText> labelText = answerConfig.getLabelText();
      if (labelText.isPresent()) {
        return createAnswerViewFromLabelText(answerConfig, labelText.get(), html);
      } else {
        throw new BusinessRuleException("Both answerConfig.getAssociatedControlEntryConfig and answerConfig.getLabelText are absent.");
      }
    }
  }

  private AnswerView createAnswerViewFromControlEntryConfig(AnswerConfig answerConfig,
                                                            ControlEntryConfig controlEntryConfig, boolean html) {
    String prompt;
    if (html) {
      prompt = htmlRenderService.convertRichTextToHtml(controlEntryConfig.getFullDescription());
    } else {
      prompt = htmlRenderService.convertRichTextToPlainText(controlEntryConfig.getFullDescription());
    }
    List<RichText> richTextList = new ArrayList<>();
    richTextList.add(controlEntryConfig.getFullDescription());
    List<SubAnswerView> subAnswerViews;
    if (answerConfig.getNestedContent().isPresent()) {
      RichText nestedContent = answerConfig.getNestedContent().get();
      richTextList.add(nestedContent);
      subAnswerViews = new ArrayList<>();
    } else {
      List<SubAnswer> subAnswers = createSubAnswers(controlEntryConfig);
      subAnswerViews = createSubAnswerViews(subAnswers, html);
      List<RichText> subAnswerDefinitions = createSubAnswerDefinitions(subAnswers);
      richTextList.addAll(subAnswerDefinitions);
    }
    String definitions = htmlRenderService.createDefinitions(richTextList);
    String relatedItems = htmlRenderService.createRelatedItemsHtml(richTextList);
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
    String nestedContent = createdNestedContent(answerConfig, html);
    boolean detailPanel = hasDetailPanel(moreInformation, definitions, relatedItems);
    return new AnswerView(prompt, answerConfig.getAnswerId(), answerConfig.isDividerAbove(), subAnswerViews,
        nestedContent, moreInformation, definitions, relatedItems, detailPanel);
  }

  private AnswerView createAnswerViewFromLabelText(AnswerConfig answerConfig, RichText labelText, boolean html) {
    String prompt;
    if (html) {
      prompt = htmlRenderService.convertRichTextToHtml(labelText);
    } else {
      prompt = htmlRenderService.convertRichTextToPlainText(labelText);
    }
    List<RichText> richTextList = new ArrayList<>();
    richTextList.add(labelText);
    if (answerConfig.getNestedContent().isPresent()) {
      RichText nestedContent = answerConfig.getNestedContent().get();
      richTextList.add(nestedContent);
    }
    String definitions = htmlRenderService.createDefinitions(richTextList);
    String relatedItems = htmlRenderService.createRelatedItemsHtml(richTextList);
    String nestedContent = createdNestedContent(answerConfig, html);
    String moreInformation;
    Optional<String> nextStageId = answerConfig.getNextStageId();
    if (nextStageId.isPresent()) {
      moreInformation = createMoreInformation(nextStageId.get());
    } else {
      moreInformation = "";
    }
    boolean detailPanel = hasDetailPanel(moreInformation, definitions, relatedItems);
    return new AnswerView(prompt, answerConfig.getAnswerId(), answerConfig.isDividerAbove(), new ArrayList<>(),
        nestedContent, moreInformation, definitions, relatedItems, detailPanel);
  }

  @Override
  public List<SubAnswerView> createSubAnswerViews(ControlEntryConfig controlEntryConfig, boolean html) {
    List<SubAnswer> subAnswers = createSubAnswers(controlEntryConfig);
    return createSubAnswerViews(subAnswers, html);
  }

  @Override
  public String createSubAnswerViewsHtml(List<SubAnswerView> subAnswerViews) {
    return subAnswerViewsToHtml(subAnswerViews);
  }

  private List<SubAnswerView> createSubAnswerViews(List<SubAnswer> subAnswers, boolean html) {
    return subAnswers.stream()
        .map(subAnswer -> createSubAnswerView(subAnswer, html))
        .collect(Collectors.toList());
  }

  private List<RichText> createSubAnswerDefinitions(List<SubAnswer> subAnswers) {
    return subAnswers.stream()
        .map(subAnswer -> {
          List<RichText> richTexts = new ArrayList<>();
          richTexts.add(subAnswer.getRichText());
          richTexts.addAll(createSubAnswerDefinitions(subAnswer.getSubAnswers()));
          return richTexts;
        })
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private List<SubAnswer> createSubAnswers(ControlEntryConfig controlEntryConfig) {
    if (controlEntryConfig.hasNestedChildren()) {
      return journeyConfigService.getChildRatings(controlEntryConfig).stream()
          .map(this::createSubAnswer)
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
      List<AnswerConfig> answerConfigs = stageConfig.getAnswerConfigs().stream()
          .sorted(Comparator.comparing(AnswerConfig::getDisplayOrder))
          .collect(Collectors.toList());
      for (AnswerConfig answerConfig : answerConfigs) {
        stringBuilder.append("<li>");
        Optional<ControlEntryConfig> associatedControlEntryConfig = answerConfig.getAssociatedControlEntryConfig();
        if (associatedControlEntryConfig.isPresent()) {
          ControlEntryConfig controlEntryConfig = associatedControlEntryConfig.get();
          stringBuilder.append(htmlRenderService.convertRichTextToHtml(controlEntryConfig.getFullDescription()));
          String subAnswerViewsToHtml = subAnswerViewsToHtml(createSubAnswerViews(createSubAnswers(controlEntryConfig), true));
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

  private String createdNestedContent(AnswerConfig answerConfig, boolean links) {
    Optional<RichText> nestedContentOptional = answerConfig.getNestedContent();
    if (nestedContentOptional.isPresent()) {
      RichText nestedContent = nestedContentOptional.get();
      if (links) {
        return htmlRenderService.convertRichTextToHtml(nestedContent);
      } else {
        return htmlRenderService.convertRichTextToHtml(nestedContent, HtmlRenderOption.OMIT_LINKS);
      }
    } else {
      return "";
    }
  }

  private SubAnswer createSubAnswer(ControlEntryConfig controlEntryConfig) {
    List<SubAnswer> subAnswers = createSubAnswers(controlEntryConfig);
    return new SubAnswer(controlEntryConfig.getFullDescription(), subAnswers);
  }

  private SubAnswerView createSubAnswerView(SubAnswer subAnswer, boolean html) {
    String summaryDescription;
    if (html) {
      summaryDescription = htmlRenderService.convertRichTextToHtml(subAnswer.getRichText());
    } else {
      summaryDescription = htmlRenderService.convertRichTextToPlainText(subAnswer.getRichText());
    }
    List<SubAnswerView> subAnswerViews = createSubAnswerViews(subAnswer.getSubAnswers(), html);
    return new SubAnswerView(summaryDescription, subAnswerViews);
  }

  private boolean hasDetailPanel(String moreInformation, String definitions, String relatedItems) {
    return !StringUtils.isBlank(moreInformation) || !StringUtils.isBlank(definitions) || !StringUtils.isBlank(relatedItems);
  }

}
