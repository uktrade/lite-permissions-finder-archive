package components.services;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import controllers.routes;
import models.cms.StageAnswer;
import models.cms.enums.StageAnswerOutcomeType;
import models.enums.PageType;
import models.view.BreadcrumbItemView;
import models.view.BreadcrumbView;
import models.view.NoteView;
import triage.config.ControlEntryConfig;
import triage.config.JourneyConfigService;
import triage.config.StageConfig;
import triage.text.HtmlRenderService;
import utils.PageTypeUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BreadcrumbViewServiceImpl implements BreadcrumbViewService {

  private final JourneyConfigService journeyConfigService;
  private final RenderService renderService;
  private final HtmlRenderService htmlRenderService;

  @Inject
  public BreadcrumbViewServiceImpl(JourneyConfigService journeyConfigService, RenderService renderService,
                                   HtmlRenderService htmlRenderService) {
    this.journeyConfigService = journeyConfigService;
    this.renderService = renderService;
    this.htmlRenderService = htmlRenderService;
  }

  @Override
  public BreadcrumbView createBreadcrumbView(String stageId, String sessionId) {
    StageConfig stageConfig = journeyConfigService.getStageConfigById(stageId);
    boolean decontrol = PageTypeUtil.getPageType(stageConfig) == PageType.DECONTROL;
    ControlEntryConfig controlEntryConfig = getControlEntryConfig(stageConfig);

    String decontrolUrl;
    if (decontrol) {
      decontrolUrl = createDecontrolUrl(sessionId, controlEntryConfig);
    } else {
      decontrolUrl = null;
    }

    List<BreadcrumbItemView> breadcrumbItemViews = createBreadcrumbItemViews(sessionId, controlEntryConfig);
    List<NoteView> noteViews = createNoteViews(stageId);
    return new BreadcrumbView(breadcrumbItemViews, noteViews, decontrol, decontrolUrl);
  }

  @Override
  public ControlEntryConfig getControlEntryConfig(StageConfig stageConfig) {
    Optional<ControlEntryConfig> controlEntryConfigOptional = stageConfig.getRelatedControlEntry();
    if (controlEntryConfigOptional.isPresent()) {
      return controlEntryConfigOptional.get();
    } else {
      StageConfig parentStageConfig = journeyConfigService.getStageConfigForPreviousStage(stageConfig.getStageId());
      if (parentStageConfig != null) {
        return getControlEntryConfig(parentStageConfig);
      } else {
        return null;
      }
    }
  }

  @Override
  public List<BreadcrumbItemView> createBreadcrumbItemViews(String sessionId, ControlEntryConfig controlEntryConfig) {
    List<BreadcrumbItemView> breadcrumbItemViews = new ArrayList<>();
    if (controlEntryConfig != null) {
      breadcrumbItemViews.addAll(createControlCodeBreadcrumbItemViews(sessionId, controlEntryConfig));
    }
    breadcrumbItemViews.add(new BreadcrumbItemView(null, "UK Military List", null, new ArrayList<>()));
    return Lists.reverse(breadcrumbItemViews);
  }

  private List<BreadcrumbItemView> createControlCodeBreadcrumbItemViews(String sessionId,
                                                                        ControlEntryConfig controlEntryConfig) {
    String controlCode = controlEntryConfig.getControlCode();
    List<String> stageIds = journeyConfigService.getStageIdsForControlEntry(controlEntryConfig);
    List<NoteView> noteViews = createNoteViews(stageIds);
    String description = renderService.getSummaryDescription(controlEntryConfig);
    String url = createChangeUrl(sessionId, controlEntryConfig.getId(), stageIds);
    BreadcrumbItemView breadcrumbItemView = new BreadcrumbItemView(controlCode, description, url, noteViews);
    List<BreadcrumbItemView> breadcrumbItemViews = new ArrayList<>();
    breadcrumbItemViews.add(breadcrumbItemView);
    Optional<ControlEntryConfig> parentControlEntry = controlEntryConfig.getParentControlEntry();
    parentControlEntry.ifPresent(parent -> breadcrumbItemViews.addAll(createControlCodeBreadcrumbItemViews(sessionId, parent)));
    return breadcrumbItemViews;
  }

  private String createChangeUrl(String sessionId, String controlEntryId, List<String> stageIds) {
    if (stageIds.isEmpty()) {
      List<StageAnswer> stageAnswers = journeyConfigService.getStageAnswersByControlEntryIdAndOutcomeType(
          Long.parseLong(controlEntryId), StageAnswerOutcomeType.CONTROL_ENTRY_FOUND);
      if (stageAnswers.isEmpty()) {
        return null;
      } else {
        return routes.StageController.render(Long.toString(stageAnswers.get(0).getParentStageId()), sessionId).toString();
      }
    } else {
      Optional<StageConfig> stageConfigOptional = stageIds.stream()
          .map(journeyConfigService::getStageConfigById)
          .filter(stageConfigIterate -> stageConfigIterate.getQuestionType() == StageConfig.QuestionType.STANDARD)
          .findAny()
          .map(stageConfigIterate -> journeyConfigService.getStageConfigForPreviousStage(stageConfigIterate.getStageId()))
          .map(this::getNonDecontrolStageConfig);
      if (stageConfigOptional.isPresent()) {
        StageConfig stageConfig = stageConfigOptional.get();
        return routes.StageController.render(stageConfig.getStageId(), sessionId).toString();
      } else {
        return null;
      }
    }
  }

  private StageConfig getNonDecontrolStageConfig(StageConfig stageConfig) {
    if (stageConfig.getQuestionType() == StageConfig.QuestionType.DECONTROL) {
      StageConfig parentStageConfig = journeyConfigService.getStageConfigForPreviousStage(stageConfig.getStageId());
      if (parentStageConfig != null) {
        return getNonDecontrolStageConfig(parentStageConfig);
      } else {
        return null;
      }
    } else {
      return stageConfig;
    }
  }

  private String createDecontrolUrl(String sessionId, ControlEntryConfig controlEntryConfig) {
    List<String> stageIds = journeyConfigService.getStageIdsForControlEntry(controlEntryConfig);
    StageConfig stageConfig = stageIds.stream()
        .map(journeyConfigService::getStageConfigById)
        .filter(stageConfigIterate -> stageConfigIterate.getQuestionType() == StageConfig.QuestionType.DECONTROL)
        .findAny()
        .orElse(null);
    if (stageConfig != null) {
      return routes.StageController.render(stageConfig.getStageId(), sessionId).toString();
    } else {
      return null;
    }
  }

  private List<NoteView> createNoteViews(List<String> stageIds) {
    return stageIds.stream()
        .map(this::createNoteViews)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private List<NoteView> createNoteViews(String stageId) {
    return journeyConfigService.getNoteConfigsByStageId(stageId).stream()
        .map(noteConfig -> new NoteView(htmlRenderService.convertRichText(noteConfig.getNoteText(), true)))
        .collect(Collectors.toList());
  }

}
