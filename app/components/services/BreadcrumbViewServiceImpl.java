package components.services;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import controllers.routes;
import models.cms.enums.StageAnswerOutcomeType;
import models.view.BreadcrumbItemView;
import models.view.BreadcrumbView;
import models.view.NoteView;
import triage.config.ControlEntryConfig;
import triage.config.JourneyConfigService;
import triage.config.StageConfig;
import triage.text.HtmlRenderOption;
import triage.text.HtmlRenderService;

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
  public BreadcrumbView createBreadcrumbViewFromControlEntryId(String sessionId, String controlEntryId) {
    ControlEntryConfig controlEntryConfig = journeyConfigService.getControlEntryConfigById(controlEntryId);
    List<BreadcrumbItemView> breadcrumbItemViews = createBreadcrumbItemViews(sessionId, controlEntryConfig, true);
    return new BreadcrumbView(breadcrumbItemViews, new ArrayList<>());
  }

  @Override
  public BreadcrumbView createBreadcrumbView(String stageId, String sessionId, boolean includeChangeLinks,
                                             HtmlRenderOption... htmlRenderOptions) {
    StageConfig stageConfig = journeyConfigService.getStageConfigById(stageId);
    ControlEntryConfig controlEntryConfig = getControlEntryConfig(stageConfig);

    List<BreadcrumbItemView> breadcrumbItemViews = createBreadcrumbItemViews(sessionId, controlEntryConfig, includeChangeLinks,
        htmlRenderOptions);
    List<NoteView> noteViews = createNoteViews(stageId);
    return new BreadcrumbView(breadcrumbItemViews, noteViews);
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
  public List<BreadcrumbItemView> createBreadcrumbItemViews(String sessionId, ControlEntryConfig controlEntryConfig,
                                                            boolean includeChangeLinks,
                                                            HtmlRenderOption... htmlRenderOptions) {
    List<BreadcrumbItemView> breadcrumbItemViews = new ArrayList<>();
    if (controlEntryConfig != null) {
      breadcrumbItemViews.addAll(createControlCodeBreadcrumbItemViews(sessionId, controlEntryConfig, includeChangeLinks, htmlRenderOptions));
    }
    breadcrumbItemViews.add(new BreadcrumbItemView(null, "UK Military List", null, new ArrayList<>()));
    return Lists.reverse(breadcrumbItemViews);
  }

  private List<BreadcrumbItemView> createControlCodeBreadcrumbItemViews(String sessionId,
                                                                        ControlEntryConfig controlEntryConfig,
                                                                        boolean includeChangeLinks,
                                                                        HtmlRenderOption... htmlRenderOptions) {
    String controlCode = controlEntryConfig.getControlCode();
    List<String> stageIds = journeyConfigService.getStageIdsForControlEntry(controlEntryConfig);
    List<NoteView> noteViews = createNoteViews(stageIds, htmlRenderOptions);
    String description = renderService.getSummaryDescription(controlEntryConfig, htmlRenderOptions);
    String url = null;
    if (includeChangeLinks) {
      url = createChangeUrl(sessionId, controlEntryConfig.getId(), stageIds);
    }
    BreadcrumbItemView breadcrumbItemView = new BreadcrumbItemView(controlCode, description, url, noteViews);
    List<BreadcrumbItemView> breadcrumbItemViews = new ArrayList<>();
    breadcrumbItemViews.add(breadcrumbItemView);
    Optional<ControlEntryConfig> parentControlEntry = controlEntryConfig.getParentControlEntry();
    parentControlEntry.ifPresent(parent -> breadcrumbItemViews.addAll(createControlCodeBreadcrumbItemViews(sessionId, parent, includeChangeLinks, htmlRenderOptions)));
    return breadcrumbItemViews;
  }

  private String createChangeUrl(String sessionId, String controlEntryId, List<String> stageIds) {
    if (stageIds.isEmpty()) {
      List<StageConfig> stageConfigs = journeyConfigService.getStageConfigsByControlEntryIdAndOutcomeType(
          controlEntryId, StageAnswerOutcomeType.CONTROL_ENTRY_FOUND);
      if (stageConfigs.isEmpty()) {
        return null;
      } else {
        return routes.StageController.render(stageConfigs.get(0).getStageId(), sessionId).toString();
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

  @Override
  public String createDecontrolUrl(String sessionId, ControlEntryConfig controlEntryConfig) {
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

  private List<NoteView> createNoteViews(List<String> stageIds, HtmlRenderOption... htmlRenderOptions) {
    return stageIds.stream()
        .map(stageId -> createNoteViews(stageId, htmlRenderOptions))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private List<NoteView> createNoteViews(String stageId, HtmlRenderOption... htmlRenderOptions) {
    return journeyConfigService.getNoteConfigsByStageId(stageId).stream()
        .map(noteConfig -> new NoteView(htmlRenderService.convertRichTextToHtml(noteConfig.getNoteText(), htmlRenderOptions)))
        .collect(Collectors.toList());
  }

}
