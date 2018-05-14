package components.services;

import com.google.inject.Inject;
import models.view.BreadcrumbItemView;
import models.view.BreadcrumbView;
import models.view.NoteView;
import triage.config.ControlEntryConfig;
import triage.config.JourneyConfigService;
import triage.config.StageConfig;
import triage.text.HtmlRenderService;

import java.util.ArrayList;
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
    List<NoteView> noteViews = createNoteViews(stageId);
    List<BreadcrumbItemView> breadcrumbItemViews = new ArrayList<>();
    breadcrumbItemViews.add(new BreadcrumbItemView("UK Military List", ""));
    Optional<ControlEntryConfig> relatedControlEntry = stageConfig.getRelatedControlEntry();
    relatedControlEntry.ifPresent(controlEntryConfig -> breadcrumbItemViews.addAll(createBreadcrumbItemViews(controlEntryConfig)));
    return new BreadcrumbView(breadcrumbItemViews, noteViews);
  }

  private List<BreadcrumbItemView> createBreadcrumbItemViews(ControlEntryConfig controlEntryConfig) {
    String controlCode = controlEntryConfig.getControlCode();
    BreadcrumbItemView breadcrumbItemView = new BreadcrumbItemView(controlCode, controlCode);
    List<BreadcrumbItemView> breadcrumbItemViews = new ArrayList<>();
    breadcrumbItemViews.add(breadcrumbItemView);
    Optional<ControlEntryConfig> parentControlEntry = controlEntryConfig.getParentControlEntry();
    parentControlEntry.ifPresent(parent -> breadcrumbItemViews.addAll(createBreadcrumbItemViews(parent)));
    return breadcrumbItemViews;
  }

  private List<NoteView> createNoteViews(String stageId) {
    return journeyConfigService.getNotesForStageId(stageId).stream()
        .map(noteConfig -> new NoteView(htmlRenderService.convertRichTextToPlainText(noteConfig.getNoteText())))
        .collect(Collectors.toList());
  }

}
