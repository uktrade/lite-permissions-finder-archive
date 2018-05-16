package components.services;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import models.view.BreadcrumbItemView;
import models.view.BreadcrumbView;
import models.view.NoteView;
import triage.config.ControlEntryConfig;
import triage.config.JourneyConfigService;
import triage.config.StageConfig;
import triage.text.HtmlRenderService;
import triage.text.RichText;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BreadcrumbViewServiceImpl implements BreadcrumbViewService {

  private final JourneyConfigService journeyConfigService;
  private final HtmlRenderService htmlRenderService;

  @Inject
  public BreadcrumbViewServiceImpl(JourneyConfigService journeyConfigService,
                                   HtmlRenderService htmlRenderService) {
    this.journeyConfigService = journeyConfigService;
    this.htmlRenderService = htmlRenderService;
  }

  @Override
  public BreadcrumbView createBreadcrumbView(String stageId) {
    StageConfig stageConfig = journeyConfigService.getStageConfigForStageId(stageId);
    List<BreadcrumbItemView> breadcrumbItemViews = new ArrayList<>();
    Optional<ControlEntryConfig> relatedControlEntry = stageConfig.getRelatedControlEntry();
    relatedControlEntry.ifPresent(controlEntryConfig -> breadcrumbItemViews.addAll(createBreadcrumbItemViews(controlEntryConfig)));
    breadcrumbItemViews.add(new BreadcrumbItemView("UK Military List", null, null, new ArrayList<>()));
    return new BreadcrumbView(Lists.reverse(breadcrumbItemViews));
  }

  private List<BreadcrumbItemView> createBreadcrumbItemViews(ControlEntryConfig controlEntryConfig) {
    String controlCode = controlEntryConfig.getControlCode();
    List<String> stageIds = journeyConfigService.getStageIdsForControlCode(controlEntryConfig);
    List<NoteView> noteViews = createNoteViews(stageIds);
    String description = getSummaryDescription(controlEntryConfig);
    BreadcrumbItemView breadcrumbItemView = new BreadcrumbItemView(controlCode, description, controlCode, noteViews);
    List<BreadcrumbItemView> breadcrumbItemViews = new ArrayList<>();
    breadcrumbItemViews.add(breadcrumbItemView);
    Optional<ControlEntryConfig> parentControlEntry = controlEntryConfig.getParentControlEntry();
    parentControlEntry.ifPresent(parent -> breadcrumbItemViews.addAll(createBreadcrumbItemViews(parent)));
    return breadcrumbItemViews;
  }

  private List<NoteView> createNoteViews(List<String> stageIds) {
    return stageIds.stream()
        .map(this::createNoteViews)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private List<NoteView> createNoteViews(String stageId) {
    return journeyConfigService.getNotesForStageId(stageId).stream()
        .map(noteConfig -> new NoteView(htmlRenderService.convertRichTextToPlainText(noteConfig.getNoteText())))
        .collect(Collectors.toList());
  }

  private String getSummaryDescription(ControlEntryConfig controlEntryConfig) {
    Optional<RichText> summaryDescription = controlEntryConfig.getSummaryDescription();
    if (summaryDescription.isPresent()) {
      RichText richText = summaryDescription.get();
      return htmlRenderService.convertRichTextToPlainText(richText);
    } else {
      return null;
    }
  }

}
